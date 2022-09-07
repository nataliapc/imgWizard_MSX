package org.nataliapc.imagewizard.utils

import java.util.*
import kotlin.math.pow


abstract class AsmZ80Virtual
{
    companion object {
        var carryFlag = false
        var zeroFlag = false
        var signFlag = false
        var pvFlag = false
        fun clearFlags() { carryFlag=false ; zeroFlag=false ; signFlag=false ; pvFlag=false }
        fun updatePvFlagParity(reg: Reg8) { pvFlag = (reg.get().countOneBits() % 2) == 0 }
        fun updatePvFlagParity(reg: Reg16) { pvFlag = (reg.get().countOneBits() % 2) == 0 }
        fun updatePvFlagOverflow8(value: Int) { pvFlag = (value and 0x180) != 0 }
        fun updatePvFlagOverflow16(value: Int) { pvFlag = (value and 0x18000) != 0 }
        fun updateSignFlag(reg: Reg8) { signFlag = (reg.get() and 0x80) != 0 }
        fun updateSignFlag(reg: Reg16) { signFlag = (reg.get() and 0x8000) != 0 }
        fun updateZeroFlag(reg: Reg8) { zeroFlag = reg.get() == 0 }
        fun updateZeroFlag(reg: Reg16) { zeroFlag = reg.get() == 0 }
        fun updateCarryFlag8(value: Int) { carryFlag = value < 0 || (value and 0x100) != 0 }
        fun updateCarryFlag16(value: Int) { carryFlag = value < 0 || (value and 0x10000) != 0 }
    }

    /**
     * Stack
     */
    object Z80stack {
        private val stack = Stack<Int>()
        fun push(reg: Reg16) { stack.push(reg.get()) }
        fun pop(reg: Reg16) { reg.ld(stack.pop()) }
    }

    /**
     * Register 8 bits
     */
    class Reg8() {
        private var reg8 = 0
        private var exx = 0

        constructor(value: Int) : this() { ld(value) }
        constructor(value: Byte) : this() { ld(value.toUByte().toInt()) }

        fun get(): Int = reg8
        fun ld(value: Byte) { ld(value.toUByte().toInt()) }
        fun ld(value: Int) { reg8 = value and 0xff }
        fun or(reg8: Reg8) { or(reg8.get()) }
        fun or(value: Int) {
            //C and N flags cleared, P/V detects parity, and rest are modified by definition.
            ld(get() or value)
            carryFlag = false
            updatePvFlagParity(this)
            updateSignFlag(this)
            updateZeroFlag(this)
        }
        fun inc() {
            //Preserves C flag, N flag is reset, P/V detects overflow and rest are modified by definition.
            updatePvFlagOverflow8(get() + 1)
            ld(get() + 1)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun dec() {
            //C flag preserved, P/V detects overflow and rest modified by definition.
            ld(get() - 1)
            updatePvFlagParity(this)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun add(value: Int) {
            //N flag is reset, P/V is interpreted as overflow. Rest of the flags is modified by definition.
            updateCarryFlag8(get() + value)
            updatePvFlagOverflow8(get() + value)
            ld(get() + value)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun add(reg: Reg8) { add(reg.get()) }
        fun bit(bit: Int) {
            //Opposite of the nth bit is written into the Z flag. C is preserved, N is reset, H is set, and S and P/V are undefined.
            zeroFlag = (get() and 2.0.pow(bit).toInt()) == 0
        }
        fun res(bit: Int) {
            //Flags are preserved.
            ld(get() and 2.0.pow(bit).toInt().xor(0xff))
        }
        fun rl() {
            //C is changed to the leaving 7th bit, H and N are reset, P/V is parity, S and Z are modified by definition.
            reg8 = reg8 shl 1
            reg8 = reg8 or (if (carryFlag) 1 else 0)
            updateCarryFlag8(reg8)
            ld(reg8)
            updatePvFlagParity(this)
            updateSignFlag(this)
            updateZeroFlag(this)
        }
        fun rla() {
            //C is changed to the leaving 7th bit, H and N are reset, P/V , S and Z are preserved.
            reg8 = reg8 shl 1
            reg8 = reg8 or (if (carryFlag) 1 else 0)
            updateCarryFlag8(reg8)
            ld(reg8)
        }
        fun exx() {
            val aux = exx
            exx = reg8
            reg8 = aux
        }
        override fun toString() = "%02x ${get()}".format(get())
    }

    /**
     * Register 16 bits
     */
    class Reg16() {
        var high = Reg8()
        var low = Reg8()

        constructor(h: Reg8, l: Reg8): this() { this.high = h ; this.low = l }
        constructor(value: Int) : this() { ld(value) }

        fun get(): Int = (high.get() shl 8) or low.get()
        fun ld(value: Int) {
            low.ld(value and 0xff)
            high.ld((value shr 8) and 0xff)
        }
        fun ld(reg16: Reg16) { ld(reg16.get()) }
        fun inc() {
            //No flags altered.
            low.ld(low.get() + 1)
            if (low.get()==0) high.ld(high.get() + 1)
        }
        fun dec() {
            //No flags altered.
            ld(get() - 1)
        }
        fun add(value: Int) {
            //Preserves the S, Z and P/V flags, and H is undefined. Rest of flags modified by definition.
            updateCarryFlag16(get() + value)
            ld(get() + value)
        }
        fun add(reg: Reg16) { add(reg.get()) }
        fun adc(reg: Reg16) {
            //The N flag is reset, P/V is interpreted as overflow. The rest of the flags is modified by definition.
            //In the case of 16-bit addition the H flag is undefined.
            val newValue = get() + reg.get() + if(carryFlag) 1 else 0
            updatePvFlagOverflow16(newValue)
            updateCarryFlag16(newValue)
            ld(newValue)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun sbc(reg: Reg16) {
            //N flag is set, P/V detects overflow, rest modified by definition.
            //In the case of 16-bit registers, h flag is undefined.
            val rest = reg.get() + if(carryFlag) 1 else 0
            updateCarryFlag16(get() - rest)
            updatePvFlagOverflow16(get() - rest)
            ld(get() - rest)
            updateSignFlag(this)
            updateZeroFlag(this)
        }
        fun exx() {
            high.exx()
            low.exx()
        }
        override fun toString() = "%04x ${get()}".format(get())
    }

}