package org.nataliapc.imagewizard.resourcefiles

import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.imagewizard.resourcefiles.interfaces.ResElement
import java.io.File


class ResElementFile(private val file: File, private val compressor: Compressor = Raw()): ResElement
{
    override fun getName(): String = file.name

    override fun getContent(): ByteArray = file.inputStream().use { it.readAllBytes() }

    override fun getSize(): Int = file.length().toInt()

    override fun getCompressedSize(): Int {
        throw RuntimeException("Not applicable to this type of resource. Not yet calculated")
    }

    override fun getCompressor(): Compressor = compressor

    override fun getCRC(): Long {
        throw RuntimeException("Not applicable to this type of resource. Not yet calculated")
    }
}