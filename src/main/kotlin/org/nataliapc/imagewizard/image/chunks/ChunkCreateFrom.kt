package org.nataliapc.imagewizard.image.chunks

import java.io.DataInputStream

interface ChunkCreateFrom
{
    fun from(stream: DataInputStream): Chunk
}