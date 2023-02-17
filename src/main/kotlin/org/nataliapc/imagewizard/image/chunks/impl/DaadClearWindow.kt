package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import java.io.DataInputStream


/*
    Chunk ClearWindow format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (17:ClearWindow)
        0x0001  2    Extra header length (0)
        0x0003  2    Data length (0)
 */
class DaadClearWindow : ChunkAbstractImpl(17)
{
    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): Chunk {
            val obj = DaadClearWindow()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun getInfo(): Array<String> {
        return arrayOf("DAAD Clear Window")
    }
}