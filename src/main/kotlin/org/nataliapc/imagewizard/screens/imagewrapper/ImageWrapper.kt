package org.nataliapc.imagewizard.screens.imagewrapper

import org.nataliapc.imagewizard.screens.ColorType
import org.nataliapc.imagewizard.screens.PaletteType
import org.nataliapc.imagewizard.screens.ScreenBitmap
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.ColorByteArrayOutputStream
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
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
    fun getWidth(): Int
    fun getHeight(): Int

    fun isIndexed(): Boolean
    fun isTrueColor(): Boolean
    fun setOutputFormat(colorType: ColorType, paletteType: PaletteType)
    fun getOriginalPalette(): IntArray
    fun getFinalPalette(): ByteArray
}


class ImageWrapperImpl private constructor(): ImageWrapper
{
    private lateinit var image: BufferedImage
    var colorType = defaultColorType
        private set
    var paletteType = defaultPaletteType
        private set

    override fun getWidth(): Int = image.width
    override fun getHeight(): Int = image.height

    companion object
    {
        private val defaultColorType = ColorType.BD8
        private val defaultPaletteType = PaletteType.GRB555

        fun from(file:File, screen: ScreenBitmap): ImageWrapper {
            return from(file, screen.colorType, screen.paletteType)
        }

        fun from(file: File, colorType: ColorType = defaultColorType, paletteType: PaletteType = defaultPaletteType): ImageWrapper {
            return from(file.inputStream(), colorType, paletteType)
        }

        fun from(stream: InputStream, colorType: ColorType = defaultColorType, paletteType: PaletteType = defaultPaletteType): ImageWrapper {
            val obj = ImageWrapperImpl()
            obj.colorType = colorType
            obj.paletteType = paletteType

            obj.image = ImageIO.read(stream)
            return obj
        }
    }

    override fun isIndexed() = image.type == BufferedImage.TYPE_BYTE_INDEXED || image.type == BufferedImage.TYPE_BYTE_BINARY
    override fun isTrueColor() = !isIndexed()

    override fun setOutputFormat(colorType: ColorType, paletteType: PaletteType) {
        this.colorType = colorType
        this.paletteType = paletteType
    }

    override fun getFinalPalette(): ByteArray {
        val out = DataByteArrayOutputStream()

        getOriginalPalette().forEach {
            paletteType.writeFromRGB24(it, out)
        }
        val paletteSize = (2.0.pow(colorType.bpp.toDouble()) * round((paletteType.bpp + 7) / 8.0)).toInt()
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

        val out = ColorByteArrayOutputStream(colorType, paletteType)
        val palette = if (isIndexed()) { getOriginalPalette() } else { null }
        intArray.forEach {
            out.writeColor(palette?.indexOf(it) ?: it)
        }
        out.writeFlush()

        val result = out.toByteArray()
        out.close()
        return result
    }
}
