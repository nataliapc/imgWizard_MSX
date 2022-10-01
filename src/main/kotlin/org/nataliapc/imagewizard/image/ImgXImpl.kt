package org.nataliapc.imagewizard.image

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkData
import org.nataliapc.imagewizard.image.chunks.ChunkPalette
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
import java.io.InputStream
import java.lang.RuntimeException


internal class ImgXImpl(withInfoChunk: Boolean = true): ImgX {
    private var header: String = magicHeader
    private val chunks = mutableListOf<Chunk>()
    private var infoChunk: InfoChunk? = null

    companion object {
        private const val magicHeader = "IMGX"

        fun from(file: File): ImgX {
            return from(DataByteArrayInputStream(file.inputStream()))
        }

        fun from(inputStream: InputStream): ImgX {
            val stream = DataInputStream(inputStream)
            val imgX = ImgXImpl(false)
            imgX.header = stream.readNBytes(magicHeader.length).toCharString()
            if (!imgX.header.startsWith(magicHeader.subSequence(0, 3))) {
                throw RuntimeException("Bad magic header reading IMX (${imgX.header})")
            }
            while (stream.available() > 0) {
                imgX.add(Chunk.Factory.from(stream))
            }
            // Determine InfoChunk from magicHeader
            imgX.infoChunk = InfoChunk.fromMagic(imgX.header)
            // Get InfoChunk if any
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

    override fun getInfoChunk(): InfoChunk? {
        return infoChunk
    }

    override fun setInfoChunk(infoChunk: InfoChunk) {
        if (this.infoChunk == null) {
            chunks.add(0, infoChunk)
        } else {
            chunks[0] = infoChunk
        }
        this.infoChunk = infoChunk
        infoChunk.update(this)
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

    override fun build(withInfoChunk: Boolean): ByteArray {
        val output = ByteArrayOutputStream(0)
        output.write(magicHeader.toByteArray())

        infoChunk?.update(this)

        chunks.forEach {
            output.write(it.build())
        }

        return output.toByteArray()
    }

    override fun render(verbose: Boolean): BufferedImage {
        var width = 256
        var height = 212
        var pixelType = PixelType.BD8
        var paletteType = PaletteType.GRB332

        if (infoChunk != null) {
            width = infoChunk!!.originalWidth
            height = infoChunk!!.originalHeight
            pixelType = infoChunk!!.pixelType
            paletteType = infoChunk!!.paletteType
        } else {
            println("*** Unable to obtain specific data from InfoChunk. Get defaults ***")
        }

        var chunkPalette: ChunkPalette? = null
        var chunkIndex = 0

        // Search first palette chunk if exists
        chunks.forEachIndexed { index, it ->
            if (it is ChunkPalette) {
                chunkPalette = it
                chunkIndex = index
                return@forEachIndexed
            }
        }

        val img = ImageWrapperImpl.from(
            BufferedImage(width, height, BufferedImage.TYPE_INT_RGB),
            pixelType,
            paletteType
        )

        //Render InfoBlock
        if (infoChunk != null && get(0) !is InfoChunk) {
            img.render(infoChunk!!)
            if (verbose) {
                print("Rendering ")
                infoChunk?.printInfo()
            }
        }

        //Render first palette if any
        if (chunkPalette != null) {
            if (verbose) {
                print("Rendering ")
                (chunkPalette as Chunk).printInfoWithOrdinal(chunkIndex)
            }
            img.render(chunkPalette as Chunk)
        }

        // Render all the chunks
        chunks.forEachIndexed { index, it ->
            if (verbose) {
                print("Rendering ")
                it.printInfoWithOrdinal(index)
            }
            img.render(it)
        }

        return img.getImageCopy()
    }

    override fun printInfo() {
        var uncompressedDataSize = 0
        var compressedDataSize = 0

        println("### Image type: $header")
        chunks.forEachIndexed { index, it ->
            print("    ")
            it.printInfoWithOrdinal(index)
            if (it is ChunkData) {
                compressedDataSize += it.getRawData().size
                uncompressedDataSize += it.getUncompressedData().size
            }
        }
        val percent = "%.2f".format(compressedDataSize * 100.0 / uncompressedDataSize)
        println("Total data size: $uncompressedDataSize bytes. Total compressed size: $compressedDataSize [$percent%]")
    }
}