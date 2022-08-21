package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.Image
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer


/*
    Chunk Redirect format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (255)
        0x0001  2    Chunk data length (avoiding header)
        0x0003  2    Empty chunk header (filled with 0x00)
        --data--
        0x0005  1    InfoChunk version
        0x0006  2    Chunk count (without info chunk)
 */
class InfoChunk(): ChunkAbstractImpl(255)
{
    private val infoVersion = 1
    private var chunkCount = 0

    fun update(image: Image)
    {
        chunkCount = image.chunkCount() - 1
    }

    override fun build(): ByteArray
    {
        val header = super.build()
        val data = LittleEndianByteBuffer.allocate(3)
            .put(infoVersion.toByte())
            .putShort(chunkCount.toShort())
            .array()

        return LittleEndianByteBuffer.allocate(header.size + 2 + 2 + data.size)
            .put(header)
            .putShort(data.size.toShort())
            .putShort(0)
            .put(data)
            .array()
    }
}