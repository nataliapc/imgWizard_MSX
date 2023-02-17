package org.nataliapc.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


internal class AsmReg8Test : AsmZ80Helper()
    {
        @Test
        fun reg8_ld_Ok() {
            //No flags are altered except in the cases of the I or R registers.
            val a = Reg8(0)
            val b = Reg8((-1).toByte())
            Assertions.assertEquals(0, a.get())
            Assertions.assertEquals(-1, b.getSigned())

            a.ld(10)
            Assertions.assertEquals(10, a.get())
            a.ld((-5).toByte())
            Assertions.assertEquals(-5, a.getSigned())
            a.ld(b)
            Assertions.assertEquals(-1, a.getSigned())
        }

        @Test
        fun reg8_or_Ok() {
            //C and N flags cleared, P/V detects parity, and rest are modified by definition.
            val a = Reg8(2)
            val b = Reg8(1)
            for (i in 0..0xff) {
                val valA = a.get()
                val valB = b.get()
                clearFlags()
                carryFlag = true

                a.or(b)

                Assertions.assertEquals(valA or valB, a.get(), "i = $i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i = $i")
                Assertions.assertEquals(false, carryFlag, "i = $i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i = $i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i = $i")
                a.inc()
                b.dec()
            }
        }

        @Test
        fun reg8_inc_Ok() {
            //Preserves C flag, N flag is reset, P/V detects overflow and rest are modified by definition.
            val a = Reg8(0)
            for (i in 0..0xff) {
                val valA = a.getSigned()
                clearFlags()
                carryFlag = a.get() % 2 == 0

                a.inc()

                Assertions.assertEquals((valA + 1).toByte().toInt(), a.getSigned(), "i = $i")
                Assertions.assertEquals((a.get() - 1) % 2 == 0, carryFlag, "i = $i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i = $i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i = $i")
                Assertions.assertEquals(i == 127, pvFlag, "i = $i")
            }
        }

        @Test
        fun reg8_dec_Ok() {
            //C flag preserved, P/V detects overflow and rest modified by definition.
            val a = Reg8(0)
            for (i in 0..0xff) {
                val valA = a.getSigned()
                clearFlags()
                carryFlag = a.get() % 2 == 0

                a.dec()

                Assertions.assertEquals((valA - 1).toByte().toInt(), a.getSigned(), "i = $i")
                Assertions.assertEquals((a.get() - 1) % 2 == 0, carryFlag, "i = $i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i = $i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i = $i")
                Assertions.assertEquals(i == 128, pvFlag, "i = $i")
            }
        }

        @Test
        fun reg8_add_Ok() {
            //N flag is reset, P/V is interpreted as overflow. Rest of the flags is modified by definition.
            for (i in 0..0xff) {
                a.ld(i)
                clearFlags()

                a.add(10)

                Assertions.assertEquals((i + 10) and 0xff, a.get(), "i = $i")
                Assertions.assertEquals(i == 246, zeroFlag, "i = $i")
                Assertions.assertEquals(i >= 246, carryFlag, "i = $i")
                Assertions.assertEquals(i in 118..245, signFlag, "i = $i")
                Assertions.assertEquals(i in 118..127, pvFlag, "i = $i")
            }
        }

        @Test
        fun reg8_adc_Ok() {
            //The N flag is reset, P/V is interpreted as overflow. The rest of the flags is modified by definition.
            //In the case of 16-bit addition the H flag is undefined.
            for (i in 0..0xff) {
                a.ld(0xf0)
                b.ld(i)
                carryFlag = true

                a.adc(b)

                Assertions.assertEquals((0xf0 + i + 1) and 0xff, a.get(), "i = $i")
                Assertions.assertEquals(i == 15, zeroFlag, "i = $i")
                Assertions.assertEquals(i >= 15, carryFlag, "i = $i")
                Assertions.assertEquals(i !in 15..142, signFlag, "i = $i")
                Assertions.assertEquals(i in 128..142, pvFlag, "i = $i")
            }
        }

        @Test
        fun reg8_bit_Ok() {
            //Opposite of the nth bit is written into the Z flag. C is preserved, N is reset, H is set,
            //and S and P/V are undefined.
            for (i in 0..0xff) {
                a.ld(i)
                for (j in 0..7) {
                    clearFlags()
                    carryFlag = (i + j) % 2 == 0

                    a.bit(j)

                    Assertions.assertEquals(i, a.get(), "i = $i")
                    Assertions.assertEquals((i shr j) and 1 == 0, zeroFlag, "i = $i j = $j")
                    Assertions.assertEquals((i + j) % 2 == 0, carryFlag, "i = $i j = $j")
                }
            }
        }

        @Test
        fun reg8_res_Ok() {
            //Flags are preserved.
            for (i in 0..0xff) {
                for (j in 0..7) {
                    a.ld(i)
                    carryFlag = (i + j) % 2 == 0
                    zeroFlag = carryFlag
                    signFlag = carryFlag
                    pvFlag = carryFlag

                    a.res(j)

                    Assertions.assertEquals((i + j) % 2 == 0, carryFlag, "i = $i j = $j")
                    Assertions.assertEquals(carryFlag, zeroFlag, "i = $i j = $j")
                    Assertions.assertEquals(carryFlag, signFlag, "i = $i j = $j")
                    Assertions.assertEquals(carryFlag, pvFlag, "i = $i j = $j")
                    a.bit(j)
                    Assertions.assertEquals(true, zeroFlag, "i = $i j = $j")
                }
            }
        }

        @Test
        fun reg8_rl_Ok() {
            //C is changed to the leaving 7th bit, H and N are reset, P/V is parity,
            //S and Z are modified by definition.
            val values = intArrayOf(   64,    128+1, 2,    4+1,   8+2,   16+4+1, 32+8+2, 64+16+4+1, 128+32+8+2)
            val carry = booleanArrayOf(false, false, true, false, false, false,  false,  false,     false)
            a.ld(32)

            for (i in values.indices) {
                clearFlags()
                carryFlag = i % 2 != 0
                a.rl()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i=$i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i=$i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i=$i")
            }
        }

        @Test
        fun reg8_rr_Ok() {
            //Carry becomes the bit leaving on the right, H and N flags are reset, P/V is parity,
            //S and Z are modified by definition.
            val values = intArrayOf(64, 32, 16, 8, 4, 2, 1, 0, 128)
            val carry = booleanArrayOf(false, false, false, false, false, false, false, true, false)
            a.ld(128)
            carryFlag = false

            for (i in values.indices) {
                a.rr()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i=$i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i=$i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i=$i")
            }
        }

        @Test
        fun reg8_rla_Ok() {
            //C is changed to the leaving 7th bit, H and N are reset, P/V , S and Z are preserved.
            val values = intArrayOf(   64,    128+1, 2,    4+1,   8+2,   16+4+1, 32+8+2, 64+16+4+1, 128+32+8+2)
            val carry = booleanArrayOf(false, false, true, false, false, false,  false,  false,     false)
            a.ld(32)

            for (i in values.indices) {
                val origFlag = i % 2 != 0
                carryFlag = origFlag
                zeroFlag = origFlag
                signFlag = origFlag
                pvFlag = origFlag

                a.rla()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(origFlag, zeroFlag, "i = $i")
                Assertions.assertEquals(origFlag, signFlag, "i = $i")
                Assertions.assertEquals(origFlag, pvFlag, "i = $i")
            }
        }

        @Test
        fun reg8_srl_Ok() {
            //S, H, and N flags reset, Z if result is zero, P/V set if parity is even, C from bit 0.
            val values = intArrayOf(32+128, 16+128, 8+128, 4+128, 2+128, 1+128, 0+128, 0+128)
            val carry = booleanArrayOf(false, false, false, false, false, false, true, false)
            a.ld(128+64)
            carryFlag = false

            for (i in values.indices) {
                a.srl()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i=$i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i=$i")
                Assertions.assertFalse(signFlag, "i=$i")
            }
        }

        @Test
        fun reg8_sla_Ok() {
            //S and Z by definition, H and N reset, C from bit 7, P/V set if result is even.
            val values = intArrayOf(2, 4, 8, 16, 32, 64, 128, 0, 0)
            val carry = booleanArrayOf(false, false, false, false, false, false, false, true, false)
            a.ld(1)
            carryFlag = false

            for (i in values.indices) {
                a.sla()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i=$i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i=$i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i=$i")
            }
        }

        @Test
        fun reg8_sll_Ok() {
            //S and Z by definition, H and N reset, C from bit 7, P/V set if result is even.
            val values = intArrayOf(2+1, 4+3, 8+7, 16+15, 32+31, 64+63, 128+127, 255, 255)
            val carry = booleanArrayOf(false, false, false, false, false, false, false, true, true)
            a.ld(1)
            carryFlag = false

            for (i in values.indices) {
                a.sll()

                Assertions.assertEquals(values[i], a.get(), "i=$i")
                Assertions.assertEquals(carry[i], carryFlag, "i=$i")
                Assertions.assertEquals(a.get() == 0, zeroFlag, "i=$i")
                Assertions.assertEquals(a.getSigned() < 0, signFlag, "i=$i")
                Assertions.assertEquals(getParity(a.get()), pvFlag, "i=$i")
            }
        }

        @Test
        fun reg8_exx_Ok() {
            a.ld(5)
            Assertions.assertEquals(5, a.get())
            a.exx()
            a.ld(17)
            Assertions.assertEquals(17, a.get())
            a.exx()
            Assertions.assertEquals(5, a.get())
            a.exx()
            Assertions.assertEquals(17, a.get())
        }

        private fun getParity(value: Int): Boolean = (value.countOneBits() % 2) == 0
}