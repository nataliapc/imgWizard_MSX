package org.nataliapc.imagewizard.utils.imagewrapper

import org.nataliapc.imagewizard.screens.ColorType
import org.nataliapc.imagewizard.screens.PaletteType
import org.nataliapc.imagewizard.screens.ScreenBitmap
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.writeShortLE
import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.lang.Math.*
import java.lang.RuntimeException
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.pow


interface ImageWrapper: ScreenRectangle
{
    fun isIndexed(): Boolean
    fun isTrueColor(): Boolean
    fun setOutputFormat(colorType: ColorType, paletteType: PaletteType)
    fun getOriginalPalette(): IntArray
    fun getFinalPalette(): ByteArray
}


class ImageWrapperImpl private constructor(): ImageWrapper
{
    private lateinit var image: BufferedImage
    var colorType = ColorType.BD8
        private set
    var paletteType = PaletteType.None
        private set

    companion object
    {
        fun from(file:File, screen: ScreenBitmap): ImageWrapper {
            return from(file, screen.colorType, screen.paletteType)
        }

        fun from(file: File, colorType: ColorType, paletteType: PaletteType): ImageWrapper {
            return from(file.inputStream(), colorType, paletteType)
        }

        fun from(stream: InputStream, colorType: ColorType, paletteType: PaletteType): ImageWrapper {
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
        val baos = ByteArrayOutputStream()
        val out = DataOutputStream(baos)
        var value: Int

        getOriginalPalette().forEach {
            value = paletteType.fromRGB24(it)
            if (paletteType.isByteSized()) {
                out.writeByte(value)
            }
            if (paletteType.isShortSized()) {
                out.writeShortLE(value)
            }
        }
        val paletteSize = (2.0.pow(colorType.bpp.toDouble()) * round((paletteType.bpp + 7) / 8.0)).toInt()
        val palette = baos.toByteArray().copyOf(paletteSize)
        out.close()
        baos.close()

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

    override fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray {
        TODO("Not yet implemented")
    }
}
