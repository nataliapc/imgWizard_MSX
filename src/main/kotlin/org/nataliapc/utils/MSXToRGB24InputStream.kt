package org.nataliapc.utils

import org.nataliapc.imagewizard.color.ColorYJK
import org.nataliapc.imagewizard.screens.PaletteMSX
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.RuntimeException

class MSXToRGB24InputStream(stream: InputStream, private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayInputStream(stream)
{
    init {
        if (pixelType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${pixelType.bpp}bpp no supported")
        }
        resetRead()
    }

    constructor(byteArray: ByteArray, pixelType: PixelType, paletteType: PaletteType) : this(ByteArrayInputStream(byteArray), pixelType, paletteType)

    private val byteBits = 8

    private var lastBitRead = 0
    private var currentByte: Int = 0
    private var currentPalette: PaletteMSX? = null

    fun setPalette(paletteRaw: ByteArray) {
        currentPalette = PaletteMSX.Factory.from(paletteRaw, paletteType)
    }

    fun setPalette(palette: PaletteMSX) {
        currentPalette = palette
    }

    fun readColor(): List<Int> {
        return when {
            pixelType == PixelType.BYJK -> {
                ColorYJK.getRGB24fromYJK(readUnsignedByte(), readUnsignedByte(), readUnsignedByte(), readUnsignedByte())
            }
            pixelType == PixelType.BYJKP -> {
                ColorYJK.getRGB24fromYJKP(readUnsignedByte(), readUnsignedByte(), readUnsignedByte(), readUnsignedByte(), currentPalette!!)
            }
            pixelType == PixelType.BYUV -> {
                TODO("Not yet implemented")
            }
            pixelType == PixelType.BYUVP -> {
                TODO("Not yet implemented")
            }
            pixelType.isShortSized() -> listOf(paletteType.toRGB24(readUnsignedShortLE()))
            pixelType.isByteSized() -> {
                if (pixelType.indexed || isIndexedBD8())
                    listOf(currentPalette?.getColorRGB24(unpackIndexed()) ?: unpackIndexed())
                else
                    listOf(paletteType.toRGB24(readUnsignedByte()))
            }
            else -> throw RuntimeException("Unknown pixel type to read (${pixelType.name})")
        }
    }

    override fun available(): Int {
        val bytesAvailables = super.available()
        return when {
            pixelType.isIntSized() -> bytesAvailables / 4
            pixelType.isShortSized() -> bytesAvailables / 2
            pixelType.isByteSized() -> {
                bytesAvailables * (8 / pixelType.bpp) + (lastBitRead % byteBits) / pixelType.bpp
            }
            else -> throw RuntimeException("Unknown pixel type to read (${pixelType.name})")
        }
    }

    fun resetRead() {
        lastBitRead = 0
    }

    private fun unpackIndexed(): Int {
        if (lastBitRead == 0) {
            currentByte = readUnsignedByte()
            lastBitRead = byteBits
        }
        lastBitRead -= pixelType.bpp
        val outValue = (currentByte shr lastBitRead) and pixelType.mask
        return outValue
    }

    private fun isIndexedBD8(): Boolean = pixelType == PixelType.BD8 && paletteType == PaletteType.GRB555
}