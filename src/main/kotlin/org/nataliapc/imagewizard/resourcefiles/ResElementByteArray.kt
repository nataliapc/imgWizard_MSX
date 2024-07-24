package org.nataliapc.imagewizard.resourcefiles

import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.resourcefiles.interfaces.ResElement

class ResElementByteArray(
    private val resIndex: ResFileImpl.IndexResource,
    private val compressedContent: ByteArray
): ResElement
{
    override fun getName(): String = resIndex.filename

    override fun getContent(): ByteArray = compressedContent

    override fun getSize(): Int = resIndex.rawSize

    override fun getCompressedSize(): Int = resIndex.compressedSize

    override fun getCompressor(): Compressor = resIndex.compressor

    override fun getCRC(): Long = resIndex.crc32
}