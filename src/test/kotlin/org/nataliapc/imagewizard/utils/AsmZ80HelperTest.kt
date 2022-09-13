package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


internal class AsmZ80HelperTest : AsmZ80Helper()
{
    @Test
    fun updatePvFlagParity_Reg8_Ok() {
        a.ld(0b10101010)
        updatePvFlagParity(a)
        assertTrue(pvFlag)

        a.ld(0b00101010)
        updatePvFlagParity(a)
        assertFalse(pvFlag)
    }

    @Test
    fun updatePvFlagParity_Reg16_Ok() {
        hl.ld(0b1010101010101010)
        updatePvFlagParity(hl)
        assertTrue(pvFlag)

        hl.ld(0b0010101010101010)
        updatePvFlagParity(hl)
        assertFalse(pvFlag)
    }

    @Test
    fun push_pop_Ok() {
        hl.ld(500)
        assertEquals(500, hl.get())
        push(hl)
        hl.ld(45000)
        assertEquals(45000, hl.get())
        pop(hl)
        assertEquals(500, hl.get())
    }

    @Test
    fun ldi_Ok() {
        //P/V is reset in case of overflow (if BC=0 after calling LDI).
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(1)
        pvFlag = true

        ldi()

        assertEquals(0x8001, hl.get())
        assertEquals(0x4001, de.get())
        assertEquals(0x0000, bc.get())
        assertTrue(pvFlag)
    }

    @Test
    fun ldi_Overflow() {
        //P/V is reset in case of overflow (if BC=0 after calling LDI).
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(0)
        pvFlag = true

        ldi()

        assertEquals(0x8001, hl.get())
        assertEquals(0x4001, de.get())
        assertEquals(0xffff, bc.get())
        assertFalse(pvFlag)
    }

    @Test
    fun ldd_Ok() {
        //P/V is reset in case of overflow (if BC=0 after calling LDD).
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(1)
        pvFlag = false

        ldd()

        assertEquals(0x7fff, hl.get())
        assertEquals(0x3fff, de.get())
        assertEquals(0x0000, bc.get())
        assertTrue(pvFlag)
    }

    @Test
    fun ldd_Overflow_Ok() {
        //P/V is reset in case of overflow (if BC=0 after calling LDD).
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(0x0000)
        pvFlag = true

        ldd()

        assertEquals(0x7fff, hl.get())
        assertEquals(0x3fff, de.get())
        assertEquals(0xffff, bc.get())
        assertFalse(pvFlag)
    }

    @Test
    fun ldir_Ok() {
        //P/V is reset.
        clearMemory()
        for (i in 0x0000..0x0fff) {
            memory[i+0x8000] = (i % 0xff).toByte()
        }
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(0x1000)
        pvFlag = true

        ldir()

        for (i in 0x0000..0x0fff) {
            assertEquals((i % 0xff).toByte(), memory[i+0x4000], "i = $i")
        }
        assertEquals(0x9000, hl.get())
        assertEquals(0x5000, de.get())
        assertEquals(0x0000, bc.get())
        assertFalse(pvFlag)
    }

    @Test
    fun lddr_Ok() {
        //P/V is reset.
        clearMemory()
        for (i in 0x0000..0x0fff) {
            memory[i+0x7001] = (i % 0xff).toByte()
        }
        hl.ld(0x8000)
        de.ld(0x4000)
        bc.ld(0x1000)
        pvFlag = true

        lddr()

        for (i in 0x0000..0x0fff) {
            assertEquals((i % 0xff).toByte(), memory[i+0x3001], "i = $i")
        }
        assertEquals(0x7000, hl.get())
        assertEquals(0x3000, de.get())
        assertEquals(0x0000, bc.get())
        assertFalse(pvFlag)
    }

    @Test
    fun exx_Ok() {
        //All flags preserved.
        hl.ld(20000)
        de.ld(30000)
        bc.ld(50000)
        exx()

        hl.ld(200)
        de.ld(300)
        bc.ld(500)
        exx()

        assertEquals(20000, hl.get())
        assertEquals(30000, de.get())
        assertEquals(50000, bc.get())
    }

    @Test
    fun exSPHL_Ok() {
        //Flags are preserved.
        hl.ld(20000)
        de.ld(30000)
        push(de)

        exSPHL()
        pop(de)

        assertEquals(30000, hl.get())
        assertEquals(20000, de.get())
    }

    @Test
    fun exDEHL_Ok() {
        //Flags are preserved.
        hl.ld(20000)
        de.ld(30000)

        exDEHL()

        assertEquals(30000, hl.get())
        assertEquals(20000, de.get())
    }

    private fun getParity(value: Int): Boolean = (value.countOneBits() % 2) == 0
}