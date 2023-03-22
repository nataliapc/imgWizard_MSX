package org.nataliapc.imagewizard.color

import org.nataliapc.imagewizard.screens.PaletteMSX
import kotlin.math.ceil

object ColorYJK
{
    private data class CompomentsYJK(val y1: Int, val y2: Int, val y3: Int, val y4: Int, val j: Int, val k: Int)

    fun getRGB24fromYJK(byte1: Int, byte2: Int, byte3: Int, byte4: Int): List<Int>
    {
        val components = bytesToYJKComponents(byte1, byte2, byte3, byte4)

        return listOf(
            getRGB24fromYJK(components.y1, components.j, components.k),
            getRGB24fromYJK(components.y2, components.j, components.k),
            getRGB24fromYJK(components.y3, components.j, components.k),
            getRGB24fromYJK(components.y4, components.j, components.k)
        )
    }

    fun getRGB24fromYJK(y: Int, j: Int, k: Int): Int
    {
        var r = y + j
        var g = y + k
        var b = ceil((5*y - 2*j - k) / 4.0).toInt()

        r = r * 255 / 31
        g = g * 255 / 31
        b = b * 255 / 31

        r = Integer.max(r.coerceAtMost(255), 0)
        g = Integer.max(g.coerceAtMost(255), 0)
        b = Integer.max(b.coerceAtMost(255), 0)
        return b.or(g.shl(8)).or(r.shl(16))
    }

    fun getRGB24fromYJKP(byte1: Int, byte2: Int, byte3: Int, byte4: Int, palette: PaletteMSX): List<Int>
    {
        val components = bytesToYJKComponents(byte1, byte2, byte3, byte4)

        return listOf(
            getRGB24fromYJKP(components.y1, components.j, components.k, palette),
            getRGB24fromYJKP(components.y2, components.j, components.k, palette),
            getRGB24fromYJKP(components.y3, components.j, components.k, palette),
            getRGB24fromYJKP(components.y4, components.j, components.k, palette)
        )
    }

    fun getRGB24fromYJKP(y: Int, j: Int, k: Int, palette: PaletteMSX): Int
    {
        return if (y % 2 == 0) {
            getRGB24fromYJK(y, j, k)
        } else {
            palette.getColorRGB24(y.shr(1))
        }
    }

    private fun bytesToYJKComponents(byte1: Int, byte2: Int, byte3: Int, byte4: Int): CompomentsYJK
    {
        val y1 = byte1 shr 3
        val y2 = byte2 shr 3
        val y3 = byte3 shr 3
        val y4 = byte4 shr 3

        var k = byte1 and 0b111
        k = k or ((byte2 and 0b111) shl 3)
        k = decodeTwoComplement(k, 6)

        var j = byte3 and 0b111
        j = j or ((byte4 and 0x07) shl 3)
        j = decodeTwoComplement(j, 6)

        return CompomentsYJK(y1, y2, y3, y4, j, k)
    }

    private fun decodeTwoComplement(num: Int, bits: Int): Int
    {
        val signBit = 1 shl (bits-1)
        return if (num >= signBit) {
            num - (2 * signBit)
        } else {
            num
        }
    }
}