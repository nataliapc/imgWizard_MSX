package org.nataliapc.imagewizard.screens.imagewrapper

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.*
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.Command
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.LogicalOp
import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.ScreenBitmap
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.File
import java.io.InputStream
import java.lang.Math.*
import java.lang.RuntimeException
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.ceil


interface ImageWrapper: ScreenRectangle, ScreenFullImage
{
    fun getWidth(): Int
    fun getHeight(): Int
    fun getImageCopy(): BufferedImage

    fun isIndexed(): Boolean
    fun isTrueColor(): Boolean
    fun setOutputFormat(pixelType: PixelType, paletteType: PaletteType)
    fun getOriginalPalette(): IntArray
    fun getFinalPalette(): ByteArray

    fun render(chunk: Chunk)
}


class ImageWrapperImpl private constructor(): ImageWrapper
{
    private lateinit var image: BufferedImage
    var pixelType = defaultPixelType
        private set
    var paletteType = defaultPaletteType
        private set

    override fun getWidth(): Int = image.width
    override fun getHeight(): Int = image.height
    override fun getImageCopy(): BufferedImage = image.getSubimage(0,0, image.width, image.height)

    companion object
    {
        private val defaultPixelType = PixelType.BD8
        private val defaultPaletteType = PaletteType.GRB555

        fun from(file:File, screen: ScreenBitmap): ImageWrapper {
            return from(file, screen.pixelType, screen.paletteType)
        }

        fun from(file: File, pixelType: PixelType = defaultPixelType, paletteType: PaletteType = defaultPaletteType): ImageWrapper {
            return from(file.inputStream(), pixelType, paletteType)
        }

        fun from(stream: InputStream, pixelType: PixelType = defaultPixelType, paletteType: PaletteType = defaultPaletteType): ImageWrapper {
            return from(ImageIO.read(stream), pixelType, paletteType)
        }

        fun from(image: BufferedImage, pixelType: PixelType = defaultPixelType, paletteType: PaletteType = defaultPaletteType): ImageWrapper {
            val obj = ImageWrapperImpl()
            obj.pixelType = pixelType
            obj.paletteType = paletteType

            obj.image = image
            return obj
        }
    }

    override fun isIndexed() = image.type == BufferedImage.TYPE_BYTE_INDEXED || image.type == BufferedImage.TYPE_BYTE_BINARY
    override fun isTrueColor() = !isIndexed()

    override fun setOutputFormat(pixelType: PixelType, paletteType: PaletteType) {
        this.pixelType = pixelType
        this.paletteType = paletteType
    }

    override fun getFinalPalette(): ByteArray {
        val out = RGB24ToMSXOutputStream(pixelType, paletteType)

        getOriginalPalette().forEach {
            out.writeColor(it)
        }
        val paletteSize = (2.0.pow(pixelType.bpp.toDouble()) * round((paletteType.bpp + 7) / 8.0)).toInt()
        val palette = out.toByteArray().copyOf(paletteSize)
        out.close()

        return palette
    }

    override fun getOriginalPalette(): IntArray {
        if (!isIndexed()) {
            throw RuntimeException("Image is not color indexed [#${image.type}]")
        }
        val indexColorModel = image.colorModel as IndexColorModel
        val palette = IntArray(indexColorModel.mapSize)
        indexColorModel.getRGBs(palette)
        return palette
    }

    override fun getFullImage(): ByteArray {
        return getRectangle(0, 0, image.width, image.height)
    }

    override fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray {
        if (x < 0 || y < 0 || x+w > image.width || y+h > image.height) {
            throw RuntimeException("Rectangle ($x,$y,$w,$h) not match image bounds (${image.width}x${image.height})")
        }
        val intArray = IntArray(w * h)
        for (posY in y until y+h) {
            for (posX in x until x+w) {
                intArray[posX+posY*w] = image.getRGB(posX, posY)
            }
        }

        val out = RGB24ToMSXOutputStream(pixelType, paletteType)
        val palette = if (pixelType.indexed) { getOriginalPalette() } else { null }
        intArray.forEach {
            out.writeColor(palette?.indexOf(it) ?: it)
        }
        out.writeFlush()

        val result = out.toByteArray()
        out.close()
        return result
    }

    override fun render(chunk: Chunk) {
        when (chunk) {
            is DaadClearWindow, is DaadRedirectToImage, is PauseChunk -> {}
            is InfoChunk -> {
                ImageRender.reset(chunk.pixelType, chunk.paletteType, chunk.chipset)
            }
            is DaadResetWindowGraphicPointer -> { TODO() }
            is DaadSkipBytes -> { TODO() }
            is ScreenBitmapChunk -> { TODO() }
            is ScreenPaletteChunk -> { TODO() }
            is V9990CmdChunk -> { ImageRender.commandV9990(image, chunk) }
            is V9990CmdDataChunk -> { ImageRender.commandDataV9990(image, chunk.getUncompressedData()) }
            else -> {
                throw RuntimeException("Unknown chunk type (id: ${chunk.getId()})")
            }
        }
    }
}

object ImageRender {
    private var pixelType = PixelType.BD8
    private var paletteType = PaletteType.GRB332
    private var chipset = Chipset.V9938

    private var dataRect = Rectangle(0,0,0,0)
    private var dataLogicalOp = LogicalOp.None
    private var dataOutput = DataByteArrayOutputStream()

    fun reset(pixelType: PixelType, paletteType: PaletteType, chipset: Chipset = Chipset.Unspecified) {
        this.pixelType = pixelType
        this.paletteType = paletteType
        this.chipset = chipset
    }

    fun commandDataV9990(image: BufferedImage, data: ByteArray) {
        dataOutput.write(data)
        if (checkEndData()) {
            applyDataToImage(image, dataOutput)
        }
    }

    fun commandV9990(image: BufferedImage, chunk: V9990CmdChunk)
    {
        when (chunk.cmd) {
            Command.Line, Command.Search, Command.Point, Command.Pset, Command.Advance -> { TODO() }
            Command.Stop -> { TODO() }
            Command.LMMV -> { fill(image, chunk.dx, chunk.dy, chunk.nx, chunk.ny, chunk.log, chunk.mask, chunk.foreColor) }
            Command.LMMM -> { copy(image, chunk.sx, chunk.sy, chunk.dx, chunk.dy, chunk.nx, chunk.ny, chunk.log, chunk.mask) }
            Command.LMMC -> { startSendData(chunk.dx, chunk.dy, chunk.nx, chunk.ny, chunk.log) }
            else -> {
                throw RuntimeException("Unknown V9990 Command type (${chunk.cmd.value})")
            }
        }
    }

    private fun fill(image: BufferedImage,
             dx: Int, dy: Int, nx: Int, ny: Int,
             log: LogicalOp, mask: Int,
             foreColor: Int)
    {
        if (log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${log.value})") }
        val g = image.createGraphics() as Graphics2D
        g.color = Color(paletteType.toRGB24(foreColor and mask))
        g.fillRect(dx, dy, nx, ny)
    }

    private fun copy(image: BufferedImage,
             sx: Int, sy: Int, dx: Int, dy: Int, nx: Int, ny: Int,
             log: LogicalOp, mask: Int)
    {
        if (log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${log.value})") }
        if (mask != 0xffff) { TODO("mask not implemented ($mask)") }

        val g = image.createGraphics() as Graphics2D
        g.copyArea(sx, sy, dx, dy, nx, ny)
    }

    private fun startSendData(dx: Int, dy: Int, nx: Int, ny: Int, log: LogicalOp)
    {
        if (log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${log.value})") }

        dataRect = Rectangle(dx, dy, nx, ny)
        dataLogicalOp = log
        dataOutput = DataByteArrayOutputStream()
    }

    private fun checkEndData(): Boolean {
        return dataOutput.size() >= ceil(dataRect.width * dataRect.height * pixelType.bpp / 8.0).toInt()
    }

    private fun convertData(data: DataByteArrayOutputStream): IntArray {
        val inputStream = MSXToRGB24InputStream(data.toByteArray(), pixelType, paletteType)
        val out = ArrayList<Int>(0)
        inputStream.use { inStream ->
            while (inStream.available() > 0) {
                val color = Color(inputStream.readColor(), false)
                out.add(color.rgb)
            }
        }
        return out.toIntArray()
    }

    private fun applyDataToImage(image: BufferedImage, data: DataByteArrayOutputStream) {
        val dataInt = convertData(data)
/*        for (i in 0 until dataInt.size) {
            image.setRGB(i % dataRect.width, i / dataRect.width, dataInt[i])
        }*/
        image.setRGB(
            dataRect.x, dataRect.y,
            dataRect.width, dataRect.height,
            dataInt, 0, dataRect.width)
    }

}