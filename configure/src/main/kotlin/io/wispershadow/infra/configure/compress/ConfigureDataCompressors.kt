package io.wispershadow.infra.configure.compress

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

enum class ConfigureDataCompressorType(val code: Byte) {
    NONE('0'.toByte())
}

object ConfigureDataCompressors {
    private const val COMPRESSOR_CODE_INDEX = 0
    private const val MAGIC_NUMBER_INDEX = 1

    private const val HEADER_SIZE = 2

    private const val MAGIC_NUMBER = '>'.toInt()

    private val LOADER: ServiceLoader<ConfigureDataCompressor> = ServiceLoader.load(ConfigureDataCompressor::class.java)

    private val NAME_MAPPING = mutableMapOf<String, ConfigureDataCompressor>()
    private val CODE_MAPPING = mutableMapOf<Byte, ConfigureDataCompressor>()
    private val logger: Logger = LoggerFactory.getLogger(ConfigureDataCompressors::class.java)

    init {
        LOADER.forEach { configureDataCompressor ->
            val compressorName = configureDataCompressor.name
            val compressorCode = configureDataCompressor.code
            logger.info("Loading config data compressor: name={}, code={}, compressorClass={}",
                    compressorName, compressorCode, configureDataCompressor::class.java.name)

            if (NAME_MAPPING.containsKey(compressorName)) {
                throw IllegalStateException("Duplicated configuration by name: $compressorName")
            }
            if (CODE_MAPPING.containsKey(compressorCode)) {
                throw IllegalStateException("Duplicated configuration by code: $compressorCode")
            }
            NAME_MAPPING[compressorName] = configureDataCompressor
            CODE_MAPPING[compressorCode] = configureDataCompressor
        }
    }

    fun getCompressorByName(name: String): ConfigureDataCompressor {
        return NAME_MAPPING[name]
                ?: throw IllegalArgumentException(String.format("Unknown job config compressor name %s", name))
    }

    private fun getCompressorByCode(code: Byte): ConfigureDataCompressor {
        return CODE_MAPPING[code]
                ?: throw IllegalArgumentException(String.format("Unrecognized job config compressor code %d", code))
    }

    fun compress(compressor: ConfigureDataCompressor, data: ByteArray): ByteArray {
        val outputStream = getOutputStream()
        return try {
            outputStream.write(compressor.code.toInt())
            outputStream.write(MAGIC_NUMBER)
            compressor.compress(outputStream, data, 0, data.size)
            outputStream.toByteArray()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } finally {
            releaseOutputStream(outputStream)
        }
    }

    fun decompress(data: ByteArray): ByteArray {
        val configureDataCompressor = determineCompressorToUse(data)
        return decompress(configureDataCompressor, data)
    }

    private fun determineCompressorToUse(data: ByteArray): ConfigureDataCompressor {
        if (data.size < HEADER_SIZE) {
            throw IllegalArgumentException("Invalid job config data less than 2 bytes")
        }
        val magicNumber = data[MAGIC_NUMBER_INDEX].toInt()
        require(magicNumber == MAGIC_NUMBER) { String.format("Unexpected job config magic number %d", magicNumber) }
        val code = data[COMPRESSOR_CODE_INDEX]
        return getCompressorByCode(code)
    }

    private fun decompress(compressor: ConfigureDataCompressor, data: ByteArray): ByteArray {
        val outputStream = getOutputStream()
        return try {
            compressor.decompress(outputStream, data, HEADER_SIZE, data.size - HEADER_SIZE)
            outputStream.toByteArray()
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } finally {
            releaseOutputStream(outputStream)
        }
    }

    private fun getOutputStream(): ByteArrayOutputStream {
        return ByteArrayOutputStream(1024 * 1024)
    }

    private fun releaseOutputStream(outputStream: ByteArrayOutputStream) {
    }
}