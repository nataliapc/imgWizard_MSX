package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.ImgX
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer


/*
    Chunk Info format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (128)
        0x0001  2    Chunk data length (avoiding header)
        0x0003  2    Empty chunk header (filled with 0x00)
        --data--
        0x0005  1    InfoChunk version
        0x0006  2    Chunk count (without info chunk)
 */
class DaadRedirectLocation : ChunkAbstractImpl(128)
{
    private val infoVersion = 1
    private var chunkCount = 0

    fun update(image: ImgX): Chunk
    {
        chunkCount = image.chunkCount() - 1
        return this
    }

    override fun build(): ByteArray
    {
        val data = LittleEndianByteBuffer.allocate(3)
            .put(infoVersion.toByte())
            .putShort(chunkCount.toShort())
            .array()

        return ensemble(data)
    }
}