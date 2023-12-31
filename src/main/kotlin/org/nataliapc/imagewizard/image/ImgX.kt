package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import java.awt.image.BufferedImage


interface ImgX
{
    var header: String
    fun getInfoChunk(): InfoChunk?
    fun setInfoChunk(infoChunk: InfoChunk)
    fun get(index: Int): Chunk
    fun add(chunk: Chunk): ImgX
    fun addAt(index:Int, chunk: Chunk): ImgX
    fun remove(index: Int): ImgX
    fun removeLast(): ImgX
    fun chunkCount(): Int
    fun build(withInfoChunk: Boolean = true): ByteArray
    fun render(verbose: Boolean = true): BufferedImage
    fun printInfo()
}

