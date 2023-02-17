package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import java.io.DataInputStream


/*
    Chunk DAAD Reset Window Graphic Pointer format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (16:ResetPointer)
        0x0001  2    Chunk data length (0)
        0x0003  2    Empty chunk header (0)
 */
class DaadResetWindowGraphicPointer : ChunkAbstractImpl(16)
{
    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): DaadResetWindowGraphicPointer {
            val obj = DaadResetWindowGraphicPointer()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun getInfo(): Array<String> {
        return arrayOf("DAAD Reset Window Graphic Pointer")
    }
}