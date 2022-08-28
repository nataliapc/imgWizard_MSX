package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import java.awt.image.BufferedImage

interface ImgX
{
    fun get(index: Int): Chunk
    fun add(chunk: Chunk): ImgX
    fun addAt(index:Int, chunk: Chunk): ImgX
    fun remove(index: Int): ImgX
    fun removeLast(): ImgX
    fun chunkCount(): Int
    fun build(withInfoChunk: Boolean = true): ByteArray
    fun render(): BufferedImage
    fun printInfo()
}