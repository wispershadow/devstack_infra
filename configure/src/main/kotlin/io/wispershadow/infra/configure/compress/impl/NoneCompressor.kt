package io.wispershadow.infra.configure.compress.impl

import com.google.auto.service.AutoService
import io.wispershadow.infra.configure.compress.ConfigureDataCompressor
import io.wispershadow.infra.configure.compress.ConfigureDataCompressorType
import java.io.OutputStream

@AutoService(ConfigureDataCompressor::class)
class NoneCompressor : ConfigureDataCompressor {
    override val name = ConfigureDataCompressorType.NONE.name

    override val code = ConfigureDataCompressorType.NONE.code

    override fun compress(outputStream: OutputStream, data: ByteArray, offset: Int, length: Int) {
        outputStream.write(data, offset, length)
    }

    override fun decompress(outputStream: OutputStream, data: ByteArray, offset: Int, length: Int) {
        outputStream.write(data, offset, length)
    }
}