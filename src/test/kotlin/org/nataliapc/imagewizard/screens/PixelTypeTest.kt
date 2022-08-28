package org.nataliapc.imagewizard.screens

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.screens.PixelType.*


internal class PixelTypeTest {

    @Test
    fun getBpp() {
        assertEquals(2, BP2.bpp)
        assertEquals(4, BP4.bpp)
        assertEquals(6, BP6.bpp)
        assertEquals(8, BD8.bpp)
        assertEquals(16, BD16.bpp)
        assertEquals(8, BYJK.bpp)
        assertEquals(8, BYJKP.bpp)
        assertEquals(8, BYUV.bpp)
        assertEquals(8, BYUVP.bpp)
    }

    @Test
    fun getMask() {
        assertEquals(0b11, BP2.mask)
        assertEquals(0b1111, BP4.mask)
        assertEquals(0b111111, BP6.mask)
        assertEquals(0xff, BD8.mask)
        assertEquals(0xffff, BD16.mask)
        assertEquals(0xff, BYJK.mask)
        assertEquals(0xff, BYJKP.mask)
        assertEquals(0xff, BYUV.mask)
        assertEquals(0xff, BYUVP.mask)
    }

    @Test
    fun getIndexed() {
        assertEquals(true, BP2.indexed)
        assertEquals(true, BP4.indexed)
        assertEquals(true, BP6.indexed)
        assertEquals(false, BD8.indexed)
        assertEquals(false, BD16.indexed)
        assertEquals(false, BYJK.indexed)
        assertEquals(false, BYJKP.indexed)
        assertEquals(false, BYUV.indexed)
        assertEquals(false, BYUVP.indexed)
    }
}