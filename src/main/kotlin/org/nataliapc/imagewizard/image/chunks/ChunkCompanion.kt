package org.nataliapc.imagewizard.image.chunks

import java.io.DataInputStream

interface ChunkCompanion
{
    fun from(stream: DataInputStream): Chunk
}