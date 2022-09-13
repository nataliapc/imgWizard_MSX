package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class AsmReg16Test : AsmZ80Helper()
{
    @Test
    fun reg16_inc_Ok() {
        //No flags altered.
        hl.ld(0xffff)
        for (i in 0..0xffff) {
            clearFlags()
            val expected = (hl.get() + 1) and 0xffff

            hl.inc()

            Assertions.assertEquals(expected, hl.get())
            Assertions.assertEquals(false, zeroFlag)
            Assertions.assertEquals(false, carryFlag)
            Assertions.assertEquals(false, pvFlag)
            Assertions.assertEquals(false, signFlag)
        }
    }

    @Test
    fun reg16_dec_Ok() {
        //No flags altered.
        hl.ld(0x0000)
        for (i in 0..0xffff) {
            clearFlags()
            val expected = (hl.get() - 1) and 0xffff

            hl.dec()

            Assertions.assertEquals(expected, hl.get())
            Assertions.assertEquals(false, zeroFlag)
            Assertions.assertEquals(false, carryFlag)
            Assertions.assertEquals(false, pvFlag)
        }
    }

    @Test
    fun reg16_add_Ok() {
        //preserves the S, Z and P/V flags, and H is undefined.
        //Rest of flags modified by definition.
        for (i in 0..0xffff) {
            val origFlag = i % 2 != 0
            carryFlag = origFlag
            zeroFlag = origFlag
            signFlag = origFlag
            pvFlag = origFlag
            hl.ld(0xfff0)
            de.ld(i)

            hl.add(de)

            Assertions.assertEquals((0xfff0 + i) and 0xffff, hl.get(), "i = $i")
            Assertions.assertEquals(i > 15, carryFlag, "i = $i")
            Assertions.assertEquals(origFlag, zeroFlag, "i = $i")
            Assertions.assertEquals(origFlag, signFlag, "i = $i")
            Assertions.assertEquals(origFlag, pvFlag, "i = $i")
        }
    }

    @Test
    fun reg16_adc_Ok() {
        //The N flag is reset, P/V is interpreted as overflow. The rest of the flags is modified by definition.
        //In the case of 16-bit addition the H flag is undefined.
        for (i in 0..0xffff) {
            val expected = (0xfff0 + i + 1) and 0xffff
            hl.ld(0xfff0)
            de.ld(i)
            carryFlag = true

            hl.adc(de)

            Assertions.assertEquals(expected, hl.get(), "i = $i")
            Assertions.assertEquals(i == 15, zeroFlag, "i = $i")
            Assertions.assertEquals(i > 14, carryFlag, "i = $i")
            Assertions.assertEquals(i !in 0x000f..0x800e, signFlag, "i = $i")
        }
    }

    @Test
    fun reg16_sbc_Ok() {
        //N flag is set, P/V detects overflow, rest modified by definition.
        //In the case of 16-bit registers, h flag is undefined.
        for (i in 0..0xffff) {
            val expected = (10 - i - 1) and 0xffff
            hl.ld(10)
            de.ld(i)
            carryFlag = true

            hl.sbc(de)

            Assertions.assertEquals(expected, hl.get(), "i = $i")
            Assertions.assertEquals(i == 9, zeroFlag, "i = $i")
            Assertions.assertEquals(i in 0x000a..0xffff, carryFlag, "i = $i")
            Assertions.assertEquals(i in 0x000a..0x8009, signFlag, "i = $i")
            Assertions.assertEquals(i in 0x8000..0x8009, pvFlag, "i = $i")
        }
    }

    @Test
    fun reg16_exx_Ok() {
        hl.ld(500)
        Assertions.assertEquals(500, hl.get())
        hl.exx()
        hl.ld(45000)
        Assertions.assertEquals(45000, hl.get())
        hl.exx()
        Assertions.assertEquals(500, hl.get())
        hl.exx()
        Assertions.assertEquals(45000, hl.get())
    }

    private fun getParity(value: Int): Boolean = (value.countOneBits() % 2) == 0
}