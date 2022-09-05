package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.PaletteType
import org.nataliapc.imagewizard.screens.PixelType
import java.lang.RuntimeException

class MSXToRGB24OutputStream(private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayOutputStream()
{
    init {
        resetWrite()
        if (pixelType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${pixelType.bpp}bpp no supported")
        }
    }

    fun writeColor(msxColor: Int) {
        when {
            paletteType.isShortSized() -> writeShortLE(paletteType.toRGB24(msxColor))
            paletteType.isByteSized() -> {
                if (!pixelType.indexed) {
                    writeByte(paletteType.toRGB24(msxColor))
                } else {
//                    lastBitWrited -= pixelType.bpp
//                    currentByte = currentByte or ((rgb24 and pixelType.mask) shl lastBitWrited)
//                    if (lastBitWrited == 0) {
//                        writeByte(currentByte)
//                        resetWrite()
//                    }
                    TODO()
                }
            }
            else -> throw RuntimeException("Unexpected write Color ($msxColor) for ${paletteType.name} ${pixelType.name}")
        }
    }

    fun writeFlush() {
//        if (lastBitWrited != byteBits) {
//            writeByte(currentByte)
//        }
//        resetWrite()
    }

    fun resetWrite() {
//        currentByte = 0
//        lastBitWrited = byteBits
    }

}