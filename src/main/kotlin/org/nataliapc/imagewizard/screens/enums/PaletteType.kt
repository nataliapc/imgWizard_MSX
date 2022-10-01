package org.nataliapc.imagewizard.screens.enums

import java.awt.Color
import kotlin.math.round

enum class PaletteType(val bpp: Int, private val rMask: Int, private val gMask: Int, private val bMask: Int) {
    Unspecified(0, 0,0,0),
    GRB332(8, 0b00011100,0b11100000,0b00000011),
    GRB333(12, 0b000001110000,0b011100000000,0b000000000111),
    GRB555(15, 0b0000001111100000,0b0111110000000000,0b0000000000011111);

    var mask: Int = 0
        private set
    var maxColors: Int = 0
        private set

    private val rIni: Int = rMask.countTrailingZeroBits()
    private val gIni: Int = gMask.countTrailingZeroBits()
    private val bIni: Int = bMask.countTrailingZeroBits()
    private val rMax: Int = rMask shr rIni
    private val gMax: Int = gMask shr gIni
    private val bMax: Int = bMask shr bIni

    init {
        val bits = (rMask or gMask or bMask).countOneBits()
        maxColors = 1
        for (i in 1..bits) {
            maxColors *= 2
        }

        mask = 1
        for (i in 1..bpp) {
            mask *= 2
        }
    }

    companion object {
        fun byId(id: Int): PaletteType {
            return values()[id]
        }
    }

    fun isByteSized() = bpp <= 8
    fun isShortSized() = bpp <=16 && !isByteSized()

    fun toColorMSX(rgb: Int): Int {
        val color = Color(rgb)
        return toColorMSX(color.red, color.green, color.blue)
    }

    fun toColorMSX(red: Int, green: Int, blue: Int): Int {
        rMask.countLeadingZeroBits()
        val r = round(red * rMax / 255.0).toInt() shl rIni
        val g = round(green * gMax / 255.0).toInt() shl gIni
        val b = round(blue * bMax / 255.0).toInt() shl bIni
        return r or g or b
    }

    fun toRGB24(value: Int): Int {
        val r = round(((value and rMask) shr rIni) * 255.0 / rMax).toInt() shl 16
        val g = round(((value and gMask) shr gIni) * 255.0 / gMax).toInt() shl 8
        val b = round(((value and bMask) shr bIni) * 255.0 / bMax).toInt()
        return r or g or b
    }

}