package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk

interface Image
{
    fun get(index: Int): Chunk
    fun add(chunk: Chunk): Image
    fun addAt(index:Int, chunk: Chunk): Image
    fun remove(index: Int): Image
    fun removeLast(): Image
    fun chunkCount(): Int
    fun build(withInfoChunk: Boolean = true): ByteArray
}