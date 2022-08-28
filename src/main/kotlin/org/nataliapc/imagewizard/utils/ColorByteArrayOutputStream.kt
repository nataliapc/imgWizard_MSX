package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.PixelType
import org.nataliapc.imagewizard.screens.PaletteType
import java.lang.RuntimeException


class ColorByteArrayOutputStream(val pixelType: PixelType, val paletteType: PaletteType) : DataByteArrayOutputStream()
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

    fun writeColor(value: Int) {
        if (pixelType.bpp == 16) {
            writeShortLE(paletteType.fromRGB24(value))
        } else {
            lastBitWrited -= pixelType.bpp
            currentByte = currentByte or ((value and pixelType.mask) shl lastBitWrited)
            if (lastBitWrited == 0) {
                writeByte(currentByte)
                resetWrite()
            }
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