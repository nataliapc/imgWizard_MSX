package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.imagewrapper.ImageWrapperImpl
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.toCharString
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.File
import java.lang.RuntimeException


class ImgXImpl(withInfoChunk: Boolean = true): ImgX
{
    private var header: String = magicHeader
    private val chunks = mutableListOf<Chunk>()
    private var infoChunk: InfoChunk? = null

    companion object {
        private const val magicHeader = "IMGX"

        fun from(file: File): ImgX {
            return from(DataByteArrayInputStream(file.inputStream()))
        }

        fun from(stream: DataInputStream): ImgX {
            val imgX = ImgXImpl(false)
            imgX.header = stream.readNBytes(magicHeader.length).toCharString()
            if (!imgX.header.startsWith(magicHeader.subSequence(0, 3))) {
                throw RuntimeException("Bad magic header reading IMX (${imgX.header})")
            }
            while (stream.available() > 0) {
                imgX.add(Chunk.Factory.createFromStream(stream))
            }
            val chunk = imgX.get(0)
            if (chunk is InfoChunk) {
                imgX.infoChunk = chunk
            }
            return imgX
        }
    }

    init {
        if (withInfoChunk) {
            infoChunk = InfoChunk()
            add(infoChunk!!)
        }
    }

    override fun get(index: Int): Chunk {
        return chunks[index]
    }

    override fun add(chunk: Chunk): ImgX {
        chunks.add(chunk)
        infoChunk?.update(this)
        return this
    }

    override fun addAt(index: Int, chunk: Chunk): ImgX {
        chunks.add(index, chunk)
        infoChunk?.update(this)
        return this
    }

    override fun remove(index: Int): ImgX {
        chunks.removeAt(index)
        infoChunk?.update(this)
        return this
    }

    override fun removeLast(): ImgX {
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

    override fun render(): BufferedImage {
        var width = 256
        var height = 212
        var colorType = PixelType.BD8
        var paletteType = PaletteType.GRB332

        if (infoChunk != null) {
            width = infoChunk!!.originalWidth
            height = infoChunk!!.originalHeight
            colorType = infoChunk!!.pixelType
            paletteType = infoChunk!!.paletteType
        } else {
            println("*** Unable to obtain specific data from InfoChunk. Get defaults ***")
        }

        val img = ImageWrapperImpl.from(
            BufferedImage(width, height, BufferedImage.TYPE_INT_RGB),
            colorType,
            paletteType)

        chunks.forEach {
            it.printInfo()
            img.render(it)
        }

        return img.getImageCopy()
    }

    override fun printInfo() {
        println("### Image type: $header")
        chunks.forEachIndexed { index, it ->
            print("    CHUNK #$index: ")
            it.printInfo()
        }
    }

}