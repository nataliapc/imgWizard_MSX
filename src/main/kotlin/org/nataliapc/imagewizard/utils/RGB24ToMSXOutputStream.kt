package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import java.lang.RuntimeException


class RGB24ToMSXOutputStream(private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayOutputStream()
{
    private val byteBits = 8

    private var lastBitWrited = byteBits
    private var currentByte: Int = 0

    init {
        resetWrite()
        if (pixelType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${pixelType.bpp}bpp no supported")
        }
    }

    fun writeColor(rgb24: Int) {
        when {
            paletteType.isShortSized() -> writeShortLE(paletteType.toColorMSX(rgb24))
            paletteType.isByteSized() -> {
                if (!pixelType.indexed) {
                    writeByte(paletteType.toColorMSX(rgb24))
                } else {
                    lastBitWrited -= pixelType.bpp
                    currentByte = currentByte or ((rgb24 and pixelType.mask) shl lastBitWrited)
                    if (lastBitWrited == 0) {
                        writeByte(currentByte)
                        resetWrite()
                    }
                }
            }
            else -> throw RuntimeException("Unexpected write Color ($rgb24) for ${paletteType.name} ${pixelType.name}")
        }
    }

    fun writeFlush() {
        if (lastBitWrited != byteBits) {
            writeByte(currentByte)
        }
        resetWrite()
    }

    fun resetWrite() {
        currentByte = 0
        lastBitWrited = byteBits
    }

}