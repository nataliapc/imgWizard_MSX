package org.nataliapc.imagewizard.image.chunks

interface ChunkData
{
    fun getRawData(): ByteArray
    fun getUncompressedData(): ByteArray
}