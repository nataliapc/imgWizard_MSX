package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer


/*
    Chunk V9990 Command Data RAW:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (33)
        0x0001  2    Chunk data length (max: 2043 bytes)
        0x0003  2    Uncompressed data length in bytes
        ---data---
        0x0005 ...   Image data (1-2043 bytes length)
 */
class V9990CmdDataChunk(val data: ByteArray) : ChunkAbstractImpl(33)
{
    override fun build(): ByteArray
    {
        setAuxData(data.size)
        return ensemble(data)
    }
}