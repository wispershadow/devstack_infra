package io.wispershadow.infra.raft.server.log.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.atomic.AtomicInteger

class FileRecords(
    var file: File,
    val channel: FileChannel,
    val start: Int,
    val end: Int,
    val isSlice: Boolean
) : Closeable {
    private val size = AtomicInteger()
    init {
        if (isSlice) {
            size.set(end - start)
        } else {
            if (channel.size() > Int.MAX_VALUE) {
                throw RuntimeException("The size of segment $file (${channel.size()}) " +
                        "is larger than the maximum allowed segment size of ${Int.MAX_VALUE} }")
            }
            val limit = Math.min(channel.size().toInt(), end)
            size.set(limit - start)
            channel.position(limit as Long)
        }
    }

    fun sizeInBytes(): Int {
        return size.get()
    }

    fun readInto(buffer: ByteBuffer, position: Int) {
        FileUtils.readFully(channel, buffer, (position + this.start).toLong())
        buffer.flip()
    }

    fun slice(position: Int, size: Int): FileRecords {
        if (position < 0)
            throw IllegalArgumentException("Invalid position: $position in read from $this")
        if (position > sizeInBytes() - start)
            throw IllegalArgumentException("Slice from position $position exceeds end position of $this")
        if (size < 0)
            throw IllegalArgumentException("Invalid size: $size in read from $this")

        var end = this.start + position + size
        // handle integer overflow or if end is beyond the end of the file
        if (end < 0 || end >= start + sizeInBytes())
            end = start + sizeInBytes()
        return FileRecords(file, channel, this.start + position, end, true)
    }

    fun flush() {
        channel.force(true)
    }

    override fun close() {
        flush()
        trim()
        channel.close()
    }

    fun closeHandlers() {
        channel.close()
    }

    fun deleteIfExists(): Boolean {
        FileUtils.closeQuietly(channel, "FileChannel")
        return Files.deleteIfExists(file.toPath())
    }

    fun trim() {
        truncateTo(sizeInBytes())
    }

    fun renameTo(f: File) {
        try {
            FileUtils.atomicMoveWithFallback(file.toPath(), f.toPath())
        } finally {
            this.file = f
        }
    }

    fun truncateTo(targetSize: Int): Int {
        val originalSize = sizeInBytes()
        if (targetSize > originalSize || targetSize < 0) {
            throw RuntimeException("Attempt to truncate log segment $file  to $targetSize  " +
                    "bytes failed, size of this log segment is $originalSize bytes.")
        }
        if (targetSize < channel.size().toInt()) {
            channel.truncate(targetSize.toLong())
            size.set(targetSize)
        }
        return originalSize - targetSize
    }

    fun writeTo(destChannel: GatheringByteChannel, offset: Long, length: Int): Long {
        val newSize = Math.min(channel.size(), end.toLong()) - start
        val oldSize = sizeInBytes()
        if (newSize < oldSize) {
            throw java.lang.RuntimeException(String.format("Size of FileRecords %s has been truncated during write: old size %d, new size %d",
                    file.absolutePath, oldSize, newSize))
        }
        val position = start + offset
        val count = Math.min(length, oldSize).toLong()
        return channel.transferTo(position, count, destChannel)
    }

    override fun toString(): String {
        return "FileRecords(file= $file, start= $start, end= $end)"
    }

    companion object {
        fun open(file: File, mutable: Boolean, fileAlreadyExists: Boolean, initFileSize: Int, preallocate: Boolean): FileRecords {
            val channel = FileUtils.openChannel(file, mutable, fileAlreadyExists, initFileSize, preallocate)
            val end = if (!fileAlreadyExists && preallocate) {
                0
            } else {
                Int.MAX_VALUE
            }
            return FileRecords(file, channel, 0, end, false)
        }

        fun open(file: File, fileAlreadyExists: Boolean, initFileSize: Int, preallocate: Boolean): FileRecords {
            return open(file, true, fileAlreadyExists, initFileSize, preallocate)
        }

        fun open(file: File, mutable: Boolean): FileRecords {
            return open(file, mutable, false, 0, false)
        }

        fun open(file: File): FileRecords {
            return open(file, true)
        }
    }

    class LogOffsetPosition(val offset: Long, val position: Int, val size: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LogOffsetPosition

            if (offset != other.offset) return false
            if (position != other.position) return false
            if (size != other.size) return false

            return true
        }

        override fun hashCode(): Int {
            var result = offset.hashCode()
            result = 31 * result + position
            result = 31 * result + size
            return result
        }
    }
}

object FileUtils {
    private val log: Logger = LoggerFactory.getLogger(FileUtils::class.java)

    fun readFully(channel: FileChannel, destinationBuffer: ByteBuffer, position: Long) {
        if (position < 0) {
            throw IllegalArgumentException("The file channel position cannot be negative, but it is $position")
        }
        var currentPosition = position
        var bytesRead: Int
        do {
            bytesRead = channel.read(destinationBuffer, currentPosition)
            currentPosition += bytesRead.toLong()
        } while (bytesRead != -1 && destinationBuffer.hasRemaining())
    }

    fun openChannel(
        file: File,
        mutable: Boolean,
        fileAlreadyExists: Boolean,
        initFileSize: Int,
        preallocate: Boolean
    ): FileChannel {
        return if (mutable) {
            if (fileAlreadyExists || !preallocate) {
                FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ,
                        StandardOpenOption.WRITE)
            } else {
                val randomAccessFile = RandomAccessFile(file, "rw")
                randomAccessFile.setLength(initFileSize.toLong())
                randomAccessFile.channel
            }
        } else {
            FileChannel.open(file.toPath())
        }
    }

    fun closeQuietly(closeable: AutoCloseable, name: String) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (t: Throwable) {
                log.warn("Failed to close {} with type {}", name, closeable.javaClass.name, t)
            }
        }
    }

    fun atomicMoveWithFallback(source: Path, target: Path) {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
        } catch (outer: IOException) {
            try {
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING)
                log.debug("Non-atomic move of {} to {} succeeded after atomic move failed due to {}", source, target,
                        outer.message)
            } catch (inner: IOException) {
                inner.addSuppressed(outer)
                throw inner
            }
        }
    }
}
