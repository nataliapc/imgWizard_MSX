package org.nataliapc.imagewizard.compressor

import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


/*
    Based on Pletter v0.5c1 - www.xl2s.tk
    https://github.com/nanochess/Pletter
*/
class Pletter : Compressor
{
    override val id = 2

    private class Metadata {
        var reeks: Int = 0     /* Total times that byte is repeated */
        var cpos = IntArray(7)
        var clen = IntArray(7)
    }

    // Compression per byte for each mode
    private class Pakdata {
        var cost: Int = 0
        var mode: Int = 0
        var mlen: Int = 0
    }

    private class Saves {
        var buf = ByteArray(0)
        var ep: Int = 0
        var dp: Int = 0
        var p: Int = 0
        var e: Int = 0
    }

    private val undefined: Int = -1

    private var d = ByteArray(0)
    private lateinit var m : MutableList<Metadata>
    private var sourceSize: Int = 0

    // Cost of coding a constant
    private var varcost = IntArray(65536)

    private var p = ArrayList<Array<Pakdata>>()
    private var s = Saves()

    private var savelength: Int = 0

    // Maximum offset for each mode
    private val maxlen = arrayListOf(
        128,
        128 + 128,
        512 + 128,
        1024 + 128,
        2048 + 128,
        4096 + 128,
        8192 + 128
    )

    override fun compress(data: ByteArray): ByteArray
    {
        var result: ByteArray = byteArrayOf()
        try {
            loadfile(data)
            initvarcost()
            createMetadata()
            val minbl = createPakdata()
            result = save(p[0], minbl)
        } catch(e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun createPakdata(): Int {
        var minlen = sourceSize * 1000
        var minbl = 0
        var l: Int
        for (i in 0 until 7) {
            p.add(i, Array(sourceSize + 1) { Pakdata() })
            l = getlen(p[i], i)
            if (l < minlen && i != 0) {
                minlen = l
                minbl = i
            }
        }
        return minbl
    }

    private fun loadfile(data: ByteArray)
    {
        sourceSize = data.size
        d = ByteArray(sourceSize + 1) { i -> data.getOrElse(i) { 0 } }
        m = MutableList(sourceSize + 1) { Metadata() }
    }

    // Inits output buffer
    private fun init()
    {
        s.ep = 0
        s.dp = 0
        s.p = 0
        s.e = 0
        s.buf = ByteArray(sourceSize * 2)
    }

    // Adds a zero bit to output
    private fun add0()
    {
        if (s.p == 0) {
            claimevent()
        }
        s.e *= 2
        ++s.p
        if (s.p == 8) {
            addevent()
        }
    }

    // Adds an one bit to output
    private fun add1()
    {
        if (s.p == 0) {
            claimevent()
        }
        s.e *= 2
        ++s.p
        ++s.e
        if (s.p == 8) {
            addevent()
        }
    }

    // Add a X bit to output
    private fun addbit(b: Int)
    {
        if (b != 0) {
            add1()
        } else {
            add0()
        }
    }

    // Add three bits to output
    private fun add3(b: Int)
    {
        addbit(b and 4)
        addbit(b and 2)
        addbit(b and 1)
    }

    // Add a variable number to output
    private fun addvar(i: Int)
    {
        var j = 0x8000

        // Looks for the first bit set at one
        while ((i and j) == 0) {
            j = j shr 1
        }

        // Like in floating point, the uncompressed adds an extra one
        while (j != 1) {
            j = j shr 1
            add1()
            if ((i and j) != 0) {
                add1()
            } else {
                add0()
            }
        }
        // Ending signal
        add0()
    }

    // Add a data to output
    private fun adddata(d: Byte)
    {
        s.buf[s.dp++] = d
    }

    // Dump bits buffer
    private fun addevent()
    {
        s.buf[s.ep] = s.e.toByte()
        s.p = 0
        s.e = 0
    }

    // Take note where it will save the following 8 bits
    private fun claimevent()
    {
        s.ep = s.dp
        ++s.dp
    }

    // Fill to zeros and save final file
    private fun done(): ByteArray
    {
        if (s.p != 0) {
            while (s.p != 8) {
                s.e *= 2
                s.p++
            }
            addevent()
        }
        //fwrite(s.buf, 1, s.dp, file);
        return s.buf.copyOfRange(0, s.dp)
    }

    // Take note of cost of using a variable number
    private fun initvarcost()
    {
        var v = 1
        var b = 1
        var r = 1
        var j: Int

        // Depict a one costs one bit  (0)
        // 2-3 costs three bits            (1x0)
        // 4-7 costs five bits.          (1x1x0)
        while (r != 65536) {
            j = 0
            while (j != r) {
                varcost[v++] = b
                ++j
            }
            b += 2
            r *= 2
        }
    }

    // Pre-compresses the source file
    private fun createMetadata()
    {
        var j: Int
        val last = IntArray(65536) { undefined }
        val prev = IntArray((sourceSize + 1) * 2)
        var r: Int
        var t: Int

        /* For speeding up the search for pairing strings:
        **
        ** as it advances, prev[byte] points to the string immediately previous
        ** that starts with the same two current bytes, it doesn't matter to link
        ** by individual bytes as each offset needs at least one byte.
        **
        ** last is a kind of hash table pointing to each two-byte string found. */
        for (i in 0 until sourceSize) {
            m[i].cpos[0] = 0
            m[i].clen[0] = 0
            prev[i] = last[d[i].toUByte().toInt() or (d[i + 1].toUByte().toInt() shl 8)]
            last[d[i].toUByte().toInt() or (d[i + 1].toUByte().toInt() shl 8 )] = i
        }

        // Counts the bytes repeated from each starting position
        r = undefined
        t = 0
        for (i in sourceSize -1 downTo 0) {
            if (d[i] == r.toByte()) {
                m[i].reeks = (++t)
            } else {
                r = d[i].toInt()
                m[i].reeks = 1
                t = 1
            }
        }

        // Now for each possible mode
        for (bl in 0 until 7) {
            // Process the input file
            for (i: Int in 0 until sourceSize) {
                var l: Int
                var p: Int

                p = i
                if (bl != 0) {
                    m[i].clen[bl] = m[i].clen[bl - 1]
                    m[i].cpos[bl] = m[i].cpos[bl - 1]
                    p = i - m[i].cpos[bl]
                }

                // For each repeated string
                while (prev[p] != undefined) {
                    p = prev[p]
                    if (i - p > maxlen[bl]) {   // Exceeded possible offset?
                        break                 // Yes, finish
                    }
                    l = 0
                    while (d[p + l] == d[i + l] && (i + l) < sourceSize) {
                        /* Speed up trick:
                        **   If the string that is going to replace has repeated bytes...
                        **   and the replacing string has less repeated bytes...
                        **   then it can take only existing ones and avoids the
                        **   classic problem o(n2) */
                        if (m[i + l].reeks > 1) {
                            j = m[i + l].reeks
                            if (j > m[p + l].reeks) {
                                j = m[p + l].reeks
                            }
                            l += j
                        } else {
                            ++l
                        }
                    }
                    if (l > m[i].clen[bl]) {   // Look for the longest string
                        m[i].clen[bl] = l      // Longitude
                        m[i].cpos[bl] = i - p  // Position (offset)
                    }
                }
            }
            //putchar('.');
        }
        //putchar(' ');
    }

    // Get the final size of file with a mode
    private fun getlen(p: Array<Pakdata>, mode: Int): Int
    {
        var j: Int
        var cc: Int
        var ccc: Int
        var kc: Int
        var kmode: Int
        var kl: Int

        p[sourceSize].cost = 0

        // Trick: goes from onwards to backwards, this way can know all
        // possible combinations for compressing.
        for (i in sourceSize - 1 downTo 0) {
            kmode = 0   // Literal
            kl = 0
            kc = (9 + p[i + 1].cost)

            // Test every size until getting the most short
            j = m[i].clen[0]
            while (j > 1) {
                cc = (9 + varcost[j - 1] + p[i + j].cost)
                if (cc < kc) {
                    kc = cc
                    kmode = 1      // Short offset
                    kl = j
                }
                --j
            }

            // Test all sizes until getting the most short
            j = m[i].clen[mode]
            ccc = 9 + if (mode != 1) {
                mode
            } else {
                0
            }
            while (j > 1) {
                cc = ccc + (varcost[j - 1] + p[i + j].cost)
                if (cc < kc) {
                    kc = cc
                    kmode = 2      // Long offset
                    kl = j
                }
                --j
            }
            p[i].cost = kc
            p[i].mode = kmode
            p[i].mlen = kl
        }
        return p[0].cost
    }

    // Save the compressed file
    private fun save(p: Array<Pakdata>, mode: Int): ByteArray
    {
        var i = 1
        var j: Int

        init()
        if (savelength != 0) {
            adddata((sourceSize and 255).toByte())
            adddata((sourceSize shr 8).toByte())
        }
        add3(mode - 1)
        adddata(d[0])
        while (i < sourceSize) {
            when (p[i].mode) {
                0 ->  {     // Literal
                    add0()
                    adddata(d[i])
                    ++i
                }
                1 -> {      // Short offset
                    add1()
                    addvar(p[i].mlen - 1)
                    j = m[i].cpos[0] - 1
                    adddata(j.toByte())
                    i += p[i].mlen
                }
                2 -> {      // Long offset
                    add1()
                    addvar(p[i].mlen - 1)
                    j = m[i].cpos[mode] - 1
                    adddata((128 or (j and 127)).toByte())
                    j -= 128
                    if (mode == 6) addbit(j and 4096)
                    if (mode == 5) addbit(j and 2048)
                    if (mode == 4) addbit(j and 1024)
                    if (mode == 3) addbit(j and 512)
                    if (mode == 2) {
                        addbit(j and 256)
                        addbit(j and 128)
                    }
                    i += p[i].mlen
                }
            }
        }
        repeat(34) {
            add1()
        }
        return done()
    }

    // ************************************************************************
    // Based on pletter v0.5c ASM MSX unpacker

    object stack {
        val pila = Stack<Int>()
        fun push(reg: Reg16) { pila.push(reg.get()) }
        fun pop(reg: Reg16) { reg.ld(pila.pop()) }
    }
    class Reg8() {
        private var reg8 = 0
        private var exx = 0
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

        constructor(value: Int) : this() {
            ld(value)
        }
        constructor(value: Byte) : this() {
            ld(value.toUByte().toInt())
        }
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
    class Reg16() {
        var high = Reg8()
        var low = Reg8()

        constructor(h: Reg8, l: Reg8): this() { this.high = h; this.low = l }
        constructor(value: Int) : this() {
            ld(value)
        }
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
            Reg8.updateCarryFlag16(get() + value)
            ld(get() + value)
        }
        fun add(reg: Reg16) { add(reg.get()) }
        fun adc(reg: Reg16) {
            //The N flag is reset, P/V is interpreted as overflow. The rest of the flags is modified by definition.
            //In the case of 16-bit addition the H flag is undefined.
            val newValue = get() + reg.get() + if(Reg8.carryFlag) 1 else 0
            Reg8.updatePvFlagOverflow16(newValue)
            Reg8.updateCarryFlag16(newValue)
            ld(newValue)
            Reg8.updateZeroFlag(this)
            Reg8.updateSignFlag(this)
        }
        fun sbc(reg: Reg16) {
            //N flag is set, P/V detects overflow, rest modified by definition.
            //In the case of 16-bit registers, h flag is undefined.
            val rest = reg.get() + if(Reg8.carryFlag) 1 else 0
            Reg8.updateCarryFlag16(get() - rest)
            Reg8.updatePvFlagOverflow16(get() - rest)
            ld(get() - rest)
            Reg8.updateSignFlag(this)
            Reg8.updateZeroFlag(this)
        }
        fun exx() {
            high.exx()
            low.exx()
        }
        override fun toString() = "%04x ${get()}".format(get())
    }

    /**
        Max input size: 16Kb.
        Max output size: 48Kb.
     */
    override fun uncompress(data: ByteArray): ByteArray {
        val startOut = 0x4000
        val out = ArrayList<Byte>(0)
        val a = Reg8()
        val h = Reg8() ; val l = Reg8()
        val d = Reg8() ; val e = Reg8()
        val b = Reg8() ; val c = Reg8()
        val i = Reg8() ; val x = Reg8()
        val ix = Reg16(i, x)
        val bc = Reg16(b, c)
        val hl = Reg16(h, l)   // Pointer to data (0)
        val de = Reg16(d, e)   // Pointer to out (startOut)
        de.ld(startOut)
        fun ldi() {
            //P/V is reset in case of overflow (if BC=0 after calling LDI).
            var origOffset = 0
            val orig = if (hl.get() >= startOut) { origOffset = startOut ; out.toByteArray() } else data
            out.add(de.get() - startOut, orig[hl.get() - origOffset])
            de.inc()
            hl.inc()
            Reg8.pvFlag = (bc.get() == 0)
            bc.dec()
        }
        fun ldir() {
            do {
                ldi()
            } while(bc.get() != 0)
        }
        fun exx() { bc.exx(); de.exx(); hl.exx() }
        fun getBit() {
            a.ld(data[hl.get()])                // ld a,(hl)
            hl.inc()                            // inc hl
            a.rla()                             // rla
        }
        fun getBitexx() {
            exx()                               // exx
            a.ld(data[hl.get()])                // ld a,(hl)
            hl.inc()                            // inc hl
            exx()                               // exx
            a.rla()                             // rla
        }
        fun offsak() {
            bc.inc()                            // inc bc
            stack.push(hl)                      // push hl
            exx()                               // exx
            stack.push(hl)                      // push hl
            exx()                               // exx
            hl.ld(de)                           // ld l,e | ld h,d
            hl.sbc(bc)                          // sbc hl,bc
            stack.pop(bc)                       // pop bc
            ldir()                              // ldir
            stack.pop(hl)                       // pop hl
        }
        fun mode2() {
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            if (Reg8.carryFlag) {               // jr nc,offsok
                a.or(a)                         // or a
                b.inc()                         // inc b
                c.res(7)                    // res 7, c
            }
            offsak()
        }
        fun mode3() {
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode2()
        }
        fun mode4() {
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode3()
        }
        fun mode5() {
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode4()
        }
        fun mode6() {
            a.add(a)                            // add a,a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode5()
        }
        val modes = arrayListOf({ offsak() }, { mode2() }, { mode3() }, { mode4() }, { mode5() }, { mode6() })

        a.ld(data[hl.get()])                    // ld a,(hl)
        hl.inc()                                // inc hl
        exx()                                   // exx
        de.ld(0)                          // ld de, 0
        a.add(a)                                // add a, a
        a.inc()                                 // inc a
        e.rl()                                  // rl  e
        a.add(a)                                // add a, a
        e.rl()                                  // rl  e
        a.add(a)                                // add a, a
        e.rl()                                  // rl  e
        e.rl()                                  // rl  e
        hl.ld(0)                          // ld hl, modes
        hl.add(de)                              // add hl, de
        ix.ld(hl.get()/2)
        e.ld(1)                           // ld e, 1
        exx()                                   // exx
        // iy.ld(loop)
//literal:
        ldi()                                   // ldi
//loop:
        loop@ while (true) {
            a.add(a)                            // add a, a
            if (Reg8.zeroFlag) getBit()         // call z,getbit
            if (!Reg8.carryFlag) {
                ldi()                           // jr nc,literal
                continue
            }
            exx()                               // exx
            hl.ld(de)                           // ld h,d | ld l,e
//getlen:
            a.add(a)                            // add a, a
            if (Reg8.zeroFlag) getBitexx()      // call z,getbitexx
            if (Reg8.carryFlag) {               // jr nc,.lenok
//.lus:
                lus@ do {
                    a.add(a)                    // add a,a
                    if (Reg8.zeroFlag) getBitexx()// call z,getbitexx
                    hl.adc(hl)                  // adc hl, hl
                    if (Reg8.carryFlag) break@loop  // ret c
                    a.add(a)                    // add a, a
                    if (Reg8.zeroFlag) getBitexx()// call z,getbitexx
                    if (!Reg8.carryFlag) break@lus // jr nc,.lenok
                    a.add(a)                    // add a, a
                    if (Reg8.zeroFlag) getBitexx()// call z,getbitexx
                    hl.adc(hl)                  // adc hl,hl
                    if (Reg8.carryFlag) break@loop// ret c
                    a.add(a)                    // add a,a
                    if (Reg8.zeroFlag) getBitexx()// call z,getbitexx
                } while (Reg8.carryFlag)        // jp c,.lus
            }
//.lenok:
            hl.inc()                            // inc hl
            exx()                               // exx
            c.ld(data[hl.get()])                // ld c, (hl)
            hl.inc()                            // inc hl
            b.ld(0)                       // ld b, #0
            c.bit(7)                        // bit 7,c
            if (Reg8.zeroFlag)
                offsak()                        // jp z,offsok
            else
                modes[ix.get()]()               // jp (ix)
            // jp (iy)
        }

        return out.toByteArray()
    }
}
