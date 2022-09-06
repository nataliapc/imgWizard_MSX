package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import java.lang.RuntimeException

internal class MSXToRGB24InputStreamTest {

    private val arrayIn = byteArrayOf(0x01, 0x72, 0x13, 0x64, 0x25, 0x56)
    private lateinit var output: ArrayList<Int>

    @BeforeEach
    fun setUp() {
        output = arrayListOf()
    }

    @Test
    fun constructor_Fail() {
        assertThrows(RuntimeException::class.java) {
            MSXToRGB24InputStream(arrayIn, PixelType.BP6, PaletteType.Unspecified)
        }
    }

    @Test
    fun readColor_BD16() {
        val stream = MSXToRGB24InputStream(arrayIn, PixelType.BD16, PaletteType.GRB555)

        while (stream.available() > 0) {
            output.add(stream.readColor())
        }

        assertArrayEquals(
            intArrayOf(0x84e608, 0x00ce9c, 0x8cad29),
            output.toIntArray())
    }

    @Test
    fun readColor_BD8_GRB332() {
        val stream = MSXToRGB24InputStream(arrayIn, PixelType.BD8, PaletteType.GRB332)

        while (stream.available() > 0) {
            output.add(stream.readColor())
        }

        assertArrayEquals(
            intArrayOf(0x000055, 0x926daa, 0x9200ff, 0x246d00, 0x242455, 0xb649aa),
            output.toIntArray())
    }

    @Test
    fun readColor_BD8_GRB555() {
        val stream = MSXToRGB24InputStream(arrayIn, PixelType.BD8, PaletteType.GRB555)

        while (stream.available() > 0) {
            output.add(stream.readColor())
        }

        assertArrayEquals(
            intArrayOf(0x01, 0x72, 0x13, 0x64, 0x25, 0x56),
            output.toIntArray())
    }

    @Test
    fun readColor_BP4() {
        val stream = MSXToRGB24InputStream(arrayIn, PixelType.BP4, PaletteType.GRB555)

        while (stream.available() > 0) {
            output.add(stream.readColor())
        }

        assertArrayEquals(
            intArrayOf(0x0, 0x1, 0x7, 0x2, 0x1, 0x3, 0x6, 0x4, 0x2, 0x5, 0x5, 0x6),
            output.toIntArray())
    }

    @Test
    fun readColor_BP2() {
        val stream = MSXToRGB24InputStream(arrayIn, PixelType.BP2, PaletteType.GRB555)

        while (stream.available() > 0) {
            output.add(stream.readColor())
        }

        assertArrayEquals(
            intArrayOf(0,0,0,1, 1,3,0,2, 0,1,0,3, 1,2,1,0, 0,2,1,1, 1,1,1,2),
            output.toIntArray())
    }
}