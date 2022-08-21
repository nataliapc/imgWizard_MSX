package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import java.io.ByteArrayOutputStream

class ImageImpl(withInfoChunk: Boolean): Image
{
    private val magicHeader = "IMGX"
    private val chunks = mutableListOf<Chunk>()
    private var infoChunk: InfoChunk? = null

    init {
        if (withInfoChunk) {
            infoChunk = InfoChunk()
            add(infoChunk!!)
        }
    }

    override fun get(index: Int): Chunk {
        return chunks[index]
    }

    override fun add(chunk: Chunk): Image {
        chunks.add(chunk)
        infoChunk?.update(this)
        return this
    }

    override fun addAt(index: Int, chunk: Chunk): Image {
        chunks.add(index, chunk)
        infoChunk?.update(this)
        return this
    }

    override fun remove(index: Int): Image {
        chunks.removeAt(index)
        infoChunk?.update(this)
        return this
    }

    override fun removeLast(): Image {
        chunks.removeLast()
        infoChunk?.update(this)
        return this
    }

    override fun chunkCount(): Int {
        return chunks.size
    }

    override fun build(withInfoChunk: Boolean): ByteArray
    {
        val output = ByteArrayOutputStream(0)
        output.write(magicHeader.toByteArray())

        infoChunk?.update(this)

        chunks.forEach {
            output.write(it.build())
        }

        return output.toByteArray()
    }

}