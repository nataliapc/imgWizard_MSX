package org.nataliapc.imagewizard.utils

import java.util.*
import kotlin.math.pow


abstract class AsmZ80Helper
{
    protected val memory = ByteArray(65536)

    val a = Reg8()
    val f = Reg8()
    val h = Reg8()
    val l = Reg8()
    val d = Reg8()
    val e = Reg8()
    val b = Reg8()
    val c = Reg8()
    val i = Reg8()
    val x = Reg8()
    val y = Reg8()
    val ix = Reg16(i, x)
    val iy = Reg16(i, y)
    val af = Reg16(a, f)
    val bc = Reg16(b, c)
    val hl = Reg16(h, l)
    val de = Reg16(d, e)

    companion object {
        var carryFlag = false
        var zeroFlag = false
        var signFlag = false
        var pvFlag = false
        fun clearFlags() { carryFlag=false ; zeroFlag=false ; signFlag=false ; pvFlag=false }
        fun updatePvFlagParity(reg: Reg8) { pvFlag = (reg.get().countOneBits() % 2) == 0 }
        fun updatePvFlagParity(reg: Reg16) { pvFlag = (reg.get().countOneBits() % 2) == 0 }
        fun updatePvFlagOverflow8(signedValue: Int) { pvFlag = signedValue < -128 || signedValue > 127 }
        fun updatePvFlagOverflow16(signedValue: Int) { pvFlag = signedValue < -32768 || signedValue > 32767 }
        fun updateSignFlag(reg: Reg8) { signFlag = (reg.get() and 0x80) != 0 }
        fun updateSignFlag(reg: Reg16) { signFlag = (reg.get() and 0x8000) != 0 }
        fun updateZeroFlag(reg: Reg8) { zeroFlag = reg.get() == 0 }
        fun updateZeroFlag(reg: Reg16) { zeroFlag = reg.get() == 0 }
        fun updateCarryFlag8(value: Int) { carryFlag = value < 0 || (value and 0x100) != 0 }
        fun updateCarryFlag16(value: Int) { carryFlag = value < 0 || (value and 0x10000) != 0 }
    }

    init {
        clearRegs()
        clearMemory()
    }

    fun clearRegs() {
        af.ld(0); bc.ld(0); de.ld(0); hl.ld(0)
        ix.ld(0); iy.ld(0)
    }

    fun clearMemory() {
        memory.fill(0)
    }

    /**
     * Stack
     */
    protected object StackZ80 {
        private val stack = Stack<Int>()
        fun push(reg: Reg16) { stack.push(reg.get()) }
        fun pop(reg: Reg16) { reg.ld(stack.pop()) }
        fun pop(): Int = stack.pop()
    }

    /**
     * Register 8 bits
     */
    class Reg8() {
        private var reg8 = 0
        private var exx = 0

        fun toHex(): String = "%02x".format(get())

        constructor(value: Int) : this() { ld(value) }
        constructor(value: Byte) : this() { ld(value.toUByte().toInt()) }

        fun get(): Int = reg8
        fun getSigned(): Int = reg8.toByte().toInt()
        fun ld(value: Byte) { ld(value.toUByte().toInt()) }
        fun ld(value: Int) { reg8 = value and 0xff }
        fun ld(reg: Reg8) { ld(reg.get()) }
        fun or(reg: Reg8) { or(reg.get()) }
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
            updatePvFlagOverflow8(getSigned() + 1)
            ld(get() + 1)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun dec() {
            //C flag preserved, P/V detects overflow and rest modified by definition.
            updatePvFlagOverflow8(getSigned() - 1)
            ld(get() - 1)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun add(value: Int) {
            //N flag is reset, P/V is interpreted as overflow. Rest of the flags is modified by definition.
            val newValue = get() + value
            val newSignedValue = getSigned() + value.toByte().toInt()
            updateCarryFlag8(newValue)
            updatePvFlagOverflow8(newSignedValue)
            ld(newValue)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun add(reg: Reg8) { add(reg.get()) }
        fun adc(reg: Reg8) {
            //The N flag is reset, P/V is interpreted as overflow. The rest of the flags is modified by definition.
            //In the case of 16-bit addition the H flag is undefined.
            val newValue = get() + reg.get() + if(carryFlag) 1 else 0
            val newSignedValue = getSigned() + reg.getSigned() + if(carryFlag) 1 else 0
            updatePvFlagOverflow8(newSignedValue)
            updateCarryFlag8(newValue)
            ld(newValue)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
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
        fun rr() {
            //Carry becomes the bit leaving on the right, H and N flags are reset, P/V is parity, S and Z are modified by definition.
            val tempCarryFlag = reg8 and 1 == 1
            reg8 = reg8 shr 1
            reg8 = reg8 or (if (carryFlag) 128 else 0)
            carryFlag = tempCarryFlag
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
        fun srl() {
            //S, H, and N flags reset, Z if result is zero, P/V set if parity is even, C from bit 0.
            signFlag = false
            carryFlag = get() and 1 == 1
            val bit7 = get() and 128
            ld(get() and 0b01111111 shr 1 or bit7)
            updatePvFlagParity(this)
            updateZeroFlag(this)
        }
        fun sla() {
            //S and Z by definition, H and N reset, C from bit 7, P/V set if result is even.
            reg8 = reg8 shl 1
            updateCarryFlag8(reg8)
            ld(reg8)
            updatePvFlagParity(this)
            updateZeroFlag(this)
            updateSignFlag(this)
        }
        fun sll() {
            //S and Z by definition, H and N reset, C from bit 7, P/V set if result is even.
            sla()
            reg8 = reg8 or 1
            updatePvFlagParity(this)
            updateZeroFlag(this)
            updateSignFlag(this)
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

        fun toHex(): String = "%04x".format(get())

        fun get(): Int = (high.get() shl 8) or low.get()
        fun getSigned(): Int = ((high.get() shl 8) or low.get()).toShort().toInt()
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
            //preserves the S, Z and P/V flags, and H is undefined.
            //Rest of flags modified by definition.
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
            val newSignedValue = getSigned() - reg.getSigned() - if(carryFlag) 1 else 0
            updateCarryFlag16(get() - rest)
            updatePvFlagOverflow16(newSignedValue)
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

    fun push(reg: Reg16) {
        StackZ80.push(reg)
    }

    fun pop(reg: Reg16) {
        StackZ80.pop(reg)
    }

    fun ldi() {
        //P/V is reset in case of overflow (if BC=0 after calling LDI).
        memory[de.get()] = memory[hl.get()]
        de.inc()
        hl.inc()
        pvFlag = bc.get() != 0
        bc.dec()
    }

    fun ldd() {
        //P/V is reset in case of overflow (if BC=0 after calling LDD).
        memory[de.get()] = memory[hl.get()]
        de.dec()
        hl.dec()
        pvFlag = bc.get() != 0
        bc.dec()
    }

    fun ldir() {
        //P/V is reset.
        do {
            ldi()
        } while (bc.get() != 0)
        pvFlag = false
    }

    fun lddr() {
        //P/V is reset.
        do {
            ldd()
        } while (bc.get() != 0)
        pvFlag = false
    }

    fun exx() {
        bc.exx(); de.exx(); hl.exx()
    }

    fun exSPHL() {
        val aux = StackZ80.pop()
        StackZ80.push(hl)
        hl.ld(aux)
    }

    fun exDEHL() {
        val aux = de.get()
        de.ld(hl)
        hl.ld(aux)
    }

}