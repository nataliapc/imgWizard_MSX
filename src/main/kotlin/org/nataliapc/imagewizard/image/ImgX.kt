package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

interface ImgX
{
    fun getInfoChunk(): InfoChunk?
    fun get(index: Int): Chunk
    fun add(chunk: Chunk): ImgX
    fun addAt(index:Int, chunk: Chunk): ImgX
    fun remove(index: Int): ImgX
    fun removeLast(): ImgX
    fun chunkCount(): Int
    fun build(withInfoChunk: Boolean = true): ByteArray
    fun render(verbose: Boolean = true): BufferedImage
    fun printInfo()

    object Factory {
        fun getInstance(withInfoChunk: Boolean = true): ImgX = ImgXImpl(withInfoChunk)
        fun from(file: File): ImgX = ImgXImpl.from(file)
        fun from(inputStream: InputStream): ImgX = ImgXImpl.from(inputStream)
    }
}