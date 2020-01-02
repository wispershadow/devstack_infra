package io.wispershadow.infra.configure.compress

import java.io.OutputStream

interface ConfigureDataCompressor {
    /**
     * Name of this config compressor, must unique among all compressors.
     */
    val name: String

    /**
     * One byte code of this config compressor, must unique among all compressors.
     * Usually, visual ascii character for readability.
     */
    val code: Byte

    fun compress(outputStream: OutputStream, data: ByteArray, offset: Int, length: Int)

    fun decompress(outputStream: OutputStream, data: ByteArray, offset: Int, length: Int)
}