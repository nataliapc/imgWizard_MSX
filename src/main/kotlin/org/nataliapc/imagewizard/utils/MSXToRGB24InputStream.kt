package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.RuntimeException

class MSXToRGB24InputStream(stream: InputStream, private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayInputStream(stream)
{
    constructor(byteArray: ByteArray, pixelType: PixelType, paletteType: PaletteType) : this(ByteArrayInputStream(byteArray), pixelType, paletteType)

    private val byteBits = 8

    private var lastBitRead = 0
    private var currentByte: Int = 0
    private var currentPalette: IntArray? = null

    init {
        if (pixelType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${pixelType.bpp}bpp no supported")
        }
        resetRead()
    }

    fun setPalette(palette: ByteArray) {
        if (32 != palette.size) {
            throw RuntimeException("Expected palette size is 32 but is ${palette.size}")
        }

        val colorList = mutableListOf<Int>()
        DataByteArrayInputStream(palette).use {
            for (index in 0..15) {
                val originalColor = it.readUnsignedShortLE()
                colorList.add(paletteType.toRGB24(originalColor))
            }
        }
        currentPalette = colorList.toIntArray()
    }

    fun readColor(): Int {
        return when {
            pixelType.isShortSized() -> paletteType.toRGB24(readUnsignedShortLE())
            pixelType.isByteSized() -> {
                if (pixelType.indexed || isIndexedBD8())
                    if (currentPalette != null) {
                        currentPalette!![unpackIndexed()]
                    } else {
                        unpackIndexed()
                    }
                else
                    paletteType.toRGB24(readUnsignedByte())
            }
            else -> throw RuntimeException("Unknown palette type to read")
        }
    }

    override fun available(): Int {
        val bytesAvailables = super.available()
        return when {
            pixelType.isShortSized() -> bytesAvailables / 2
            pixelType.isByteSized() -> {
                bytesAvailables * (8 / pixelType.bpp) + (lastBitRead % byteBits) / pixelType.bpp
            }
            else -> throw RuntimeException("Unknown palette type to read")
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