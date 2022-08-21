package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.ColorType
import org.nataliapc.imagewizard.screens.PaletteType
import java.lang.RuntimeException


class ColorByteArrayOutputStream(val colorType: ColorType, val paletteType: PaletteType) : DataByteArrayOutputStream()
{
    private val byteBits = 8

    private var lastBitWrited = byteBits
    private var currentByte: Int = 0

    init {
        resetWrite()
        if (colorType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${colorType.bpp}bpp no supported")
        }
    }

    fun writeColor(value: Int) {
        if (colorType.bpp == 16) {
            writeShortLE(value)
        } else {
            lastBitWrited -= colorType.bpp
            currentByte = currentByte or ((value and colorType.mask) shl lastBitWrited)
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