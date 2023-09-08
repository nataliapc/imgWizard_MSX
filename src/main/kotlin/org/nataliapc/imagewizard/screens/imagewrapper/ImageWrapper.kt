package org.nataliapc.imagewizard.screens.imagewrapper

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.*
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.CommandType
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.LogicalOp
import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.ScreenBitmap
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.utils.*
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


interface ImageWrapper: ScreenRectangle, ScreenFullImage
{
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
    private var dataPointer: Int = 0

    var pixelType = defaultPixelType
        private set
    var paletteType = defaultPaletteType
        private set

    override val width: Int
        get() = image.width
    override val height: Int
        get() = image.height

    private val render = ImageRender()

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
        for (posY in 0 until h) {
            for (posX in 0 until w) {
                intArray[posX+posY*w] = getPixel(x+posX, y+posY)
            }
        }

        val out = RGB24ToMSXOutputStream(pixelType, paletteType)
        val palette = if (pixelType.indexed) { getOriginalPalette() } else { null }
        intArray.forEach {
            out.writeColor(palette?.indexOf(it) ?: it)
        }
        out.close()

        return out.toByteArray()
    }

    override fun getPixel(x: Int, y: Int): Int {
        return image.getRGB(x, y)
    }

    override fun getFullBytesFor(x: Int, y: Int): Int {
        TODO("Not yet implemented")
    }

    override fun render(chunk: Chunk) {
        when (chunk) {
            is DaadRedirectToImage, is PauseChunk -> {}
            is InfoChunk -> {
                render.reset(chunk.pixelType, chunk.paletteType, chunk.chipset)
            }
            is DaadResetWindowGraphicPointer -> { dataPointer = 0 }
            is DaadClearWindow -> { dataPointer = 0 ; render.clear(image, 0) }
            is DaadSkipBytes -> { dataPointer += chunk.skipBytes }
            is ScreenBitmapChunk -> { dataPointer = render.screenBitmapChunk(image, chunk, dataPointer) }
            is ScreenPaletteChunk -> { render.screenPalette(chunk) }
            is V9990CmdChunk -> { render.commandV9990(image, chunk) }
            is V9990CmdDataChunk -> { render.commandDataV9990(image, chunk.getUncompressedData()) }
            else -> {
                throw RuntimeException("Unknown chunk type (id: ${chunk.getId()})")
            }
        }
    }
}

class ImageRender(
    private var pixelType: PixelType = PixelType.BD8,
    private var paletteType: PaletteType = PaletteType.GRB332,
    private var chipset: Chipset = Chipset.Unspecified
) {
    private var currentPalette: ByteArray? = null

    private var dataRect = Rectangle(0,0,0,0)
    private var dataLogicalOp = LogicalOp.None
    private var dataOutput = DataByteArrayOutputStream()

    fun reset(pixelType: PixelType, paletteType: PaletteType, chipset: Chipset = Chipset.Unspecified) {
        this.pixelType = pixelType
        this.paletteType = paletteType
        this.chipset = chipset
    }

    fun clear(image: BufferedImage, color: Int) {
        fill(image, V9990CmdChunk.CommandData(0,0, 0,0, image.width,image.height, 0, LogicalOp.IMP, 0xffff, color, 0, CommandType.LMMV))
    }

    fun screenBitmapChunk(image: BufferedImage, chunk: ScreenBitmapChunk, dataPointer: Int): Int {
        val startRawPixel: Int = pixelType.pixelsPerByte.toInt() * dataPointer
        var xPos: Int = startRawPixel % image.width
        var yPos: Int = startRawPixel / image.width
        val colorStream = MSXToRGB24InputStream(chunk.getUncompressedData(), pixelType, paletteType)
        colorStream.use {
            if (currentPalette != null) {
                it.setPalette(currentPalette!!)
            }
            while (it.available() > 0) {
                it.readColor().forEach { color ->
                    image.setRGB(xPos, yPos, color)
                    xPos++
                    if (xPos >= image.width) {
                        xPos = 0
                        yPos++
                    }
                }
            }
        }
        return dataPointer + chunk.auxData
    }

    fun screenPalette(chunk: ScreenPaletteChunk) {
        currentPalette = chunk.getRawData()
    }

    fun commandDataV9990(image: BufferedImage, data: ByteArray) {
        dataOutput.write(data)
        if (checkEndData()) {
            applyDataToImage(image, dataOutput)
        }
    }

    fun commandV9990(image: BufferedImage, chunk: V9990CmdChunk)
    {
        for (i in 0 until chunk.numCommands) {
            chunk.getCommand(i).also {
                when (it.cmd) {
                    CommandType.Stop -> {}
                    CommandType.LMMV -> fill(image, it)
                    CommandType.LMMM -> copy(image, it)
                    CommandType.LMMC -> startSendData(it)
                    CommandType.BMXL -> linToRec(image, (it.sy shl 8) or it.sx, it.dx, it.dy, it.nx, it.ny)
                    CommandType.BMLX -> recToLin(image, it.sx, it.sy, it.nx, it.ny, (it.dy shl 8) or it.dx)
                    CommandType.BMLL -> linToLin(image, (it.sy shl 8) or it.sx, (it.dy shl 8) or it.dx, (it.ny shl 8) or it.nx)
                    CommandType.CMMC -> TODO("CMMC Not implemented yet")
                    CommandType.CMMM -> TODO("CMMM Not implemented yet")
                    CommandType.Line -> drawLine(image, it)
                    CommandType.Pset -> pset(image, it)
                    CommandType.Point -> TODO("Point command Not implemented")
                    CommandType.Search -> TODO("Search Not implemented yet")
                    CommandType.Advance -> TODO("Advance Not implemented yet")
                    else -> throw RuntimeException("Unknown V9990 Command type (${it.cmd.value})")
                }
            }
        }
    }

    private fun drawLine(image: BufferedImage, cmd: V9990CmdChunk.CommandData)
    {
        if (cmd.log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${cmd.log.value})") }
        val sx = cmd.dx
        val sy = cmd.dy
        val mj = cmd.nx
        val mi = cmd.ny
        val diy = ((cmd.arg.toInt() shr 3) and 1) * -2 + 1
        val dix = ((cmd.arg.toInt() shr 2) and 1) * -2 + 1
        val dx = sx + mj * dix
        val dy = sy + mi * diy

        val g = image.createGraphics() as Graphics2D
        g.color = Color(paletteType.toRGB24(cmd.foreColor and cmd.mask))
        g.drawLine(sx, sy, dx, dy)
    }

    private fun fill(image: BufferedImage, cmd: V9990CmdChunk.CommandData)
    {
        if (cmd.log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${cmd.log.value})") }
        val g = image.createGraphics() as Graphics2D
        g.color = Color(paletteType.toRGB24(cmd.foreColor and cmd.mask))
        g.fillRect(cmd.dx, cmd.dy, cmd.nx, cmd.ny)
    }

    private fun copy(image: BufferedImage, cmd: V9990CmdChunk.CommandData)
    {
        if (cmd.log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${cmd.log.value})") }
        if (cmd.mask != 0xffff) { TODO("mask not implemented ($cmd.mask)") }

        val g = image.createGraphics() as Graphics2D
        g.copyArea(cmd.sx, cmd.sy, cmd.nx, cmd.ny, cmd.dx-cmd.sx, cmd.dy-cmd.sy)
    }

    private fun pset(image: BufferedImage, cmd: V9990CmdChunk.CommandData)
    {
        if (cmd.log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${cmd.log.value})") }
        image.setRGB(cmd.dx, cmd.dy, paletteType.toRGB24(cmd.foreColor and cmd.mask))
    }

    private fun linToRec(image: BufferedImage, address: Int, dx: Int, dy: Int, nx: Int, ny: Int) {
        val bytesPerPixel = if (pixelType.isShortSized()) 2 else 1
        var sx = (address / bytesPerPixel) % image.width
        var sy = (address / bytesPerPixel) / image.width
        val pixels = ArrayList<Int>(0)

        for (pos in 0 until nx*ny) {
            pixels.add(image.getRGB(sx, sy))
            if (++sx >= image.width) { sx = 0 ; sy++ }
        }
        image.setRGB(dx, dy, nx, ny, pixels.toIntArray(), 0, nx)
    }

    private fun recToLin(image: BufferedImage, sx: Int, sy: Int, nx: Int, ny: Int, address: Int) {
        val bytesPerPixel = if (pixelType.isShortSized()) 2 else 1
        var dx = (address / bytesPerPixel) % image.width
        var dy = (address / bytesPerPixel) / image.width

        for (y in 0 until ny) {
            for (x in 0 until nx) {
                val pixel = image.getRGB(sx+x, sy+y)
                image.setRGB(dx, dy, pixel)
                if (++dx >= image.width) { dx = 0 ; dy++ }
            }
        }
    }

    private fun linToLin(image: BufferedImage, srcAddress: Int, dstAddress: Int, numBytes: Int) {
        val bytesPerPixel = if (pixelType.isShortSized()) 2 else 1
        var sx = (srcAddress / bytesPerPixel) % image.width
        var sy = (srcAddress / bytesPerPixel) / image.width
        var dx = (dstAddress / bytesPerPixel) % image.width
        var dy = (dstAddress / bytesPerPixel) / image.width
        var numPîxels = numBytes / bytesPerPixel

        while (numPîxels-- > 0) {
            val pixel = image.getRGB(sx, sy)
            image.setRGB(dx, dy, pixel)
            if (++sx >= image.width) { sx = 0 ; sy++ }
            if (++dx >= image.width) { dx = 0 ; dy++ }
        }
    }

    private fun startSendData(cmd: V9990CmdChunk.CommandData)
    {
        if (cmd.log != LogicalOp.IMP) { TODO("LogicalOp not implemented (${cmd.log.value})") }

        dataRect = Rectangle(cmd.dx, cmd.dy, cmd.nx, cmd.ny)
        dataLogicalOp = cmd.log
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
                inputStream.readColor().forEach {
                    val color = Color(it, false)
                    out.add(color.rgb)
                }
            }
        }
        return out.toIntArray()
    }

    private fun applyDataToImage(image: BufferedImage, data: DataByteArrayOutputStream) {
        val dataInt = convertData(data)
        image.setRGB(
            dataRect.x, dataRect.y,
            dataRect.width, dataRect.height,
            dataInt, 0, dataRect.width)
    }

}