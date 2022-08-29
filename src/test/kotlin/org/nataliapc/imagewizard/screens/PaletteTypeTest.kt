package org.nataliapc.imagewizard.screens

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled


internal class PaletteTypeTest {

    @Test
    fun isByteSized_Ok() {
        assertTrue(PaletteType.GRB332.isByteSized())
        assertFalse(PaletteType.GRB333.isByteSized())
        assertFalse(PaletteType.GRB555.isByteSized())
    }

    @Test
    fun isShortSized_Ok() {
        assertFalse(PaletteType.GRB332.isShortSized())
        assertTrue(PaletteType.GRB333.isShortSized())
        assertTrue(PaletteType.GRB555.isShortSized())
    }

    @Test
    fun fromRGB24_Ok() {
        assertEquals(0b10011001, PaletteType.GRB332.fromRGB24(0xffcc8844u.toInt()))
        assertEquals(0b10001100010, PaletteType.GRB333.fromRGB24(0xffcc8844u.toInt()))
        assertEquals(0b100011100101000, PaletteType.GRB555.fromRGB24(0xffcc8844u.toInt()))
    }

    @Test
    fun toRGB24_Ok() {
        assertEquals(0xdb9255, PaletteType.GRB332.toRGB24(0b10011001))
        assertEquals(0xdb9249, PaletteType.GRB333.toRGB24(0b10001100010))
        assertEquals(0xce8c42, PaletteType.GRB555.toRGB24(0b100011100101000))
    }

    @Disabled
    @Test
    fun writeFromRGB24_Ok() {
        TODO()
    }

    @Disabled
    @Test
    fun readToRGB24_Ok() {
        TODO()
    }

    @Test
    fun getBpp_Ok() {
        assertEquals(8, PaletteType.GRB332.bpp)
        assertEquals(11, PaletteType.GRB333.bpp)
        assertEquals(15, PaletteType.GRB555.bpp)
    }
}