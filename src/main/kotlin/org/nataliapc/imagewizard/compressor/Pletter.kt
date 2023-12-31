package org.nataliapc.imagewizard.compressor

import org.nataliapc.utils.AsmZ80Helper
import java.lang.Exception
import java.util.*


/*
    Based on Pletter v0.5c1 - www.xl2s.tk
    https://github.com/nanochess/Pletter
*/
@ExperimentalUnsignedTypes
class Pletter : CompressorImpl(2)
{
    private class Metadata {
        var reeks: Int = 0     /* Total times that byte is repeated */
        var cpos = IntArray(7)
        var clen = IntArray(7)
    }

    // Compression per byte for each mode
    private class Pakdata {
        var cost: UInt = 0u
        var mode: Int = 0
        var mlen: Int = 0
    }

    private class Saves {
        var buf = UByteArray(0)
        var ep: Int = 0
        var dp: Int = 0
        var p: Int = 0
        var e: Int = 0
    }

    private val undefined: Int = Int.MAX_VALUE

    private var d = UByteArray(0)
    private lateinit var m : Array<Metadata>
    private var sourceSize: Int = 0

    // Cost of coding a constant
    private var varcost = UIntArray(65536)

    private lateinit var p: Array<Array<Pakdata>>
    private var s = Saves()

    private var savelength: Int = 0

    // Maximum offset for each mode
    private val maxlen = intArrayOf(
        128,
        128 + 128,
        512 + 128,
        1024 + 128,
        2048 + 128,
        4096 + 128,
        8192 + 128
    ).toUIntArray()

    override fun compress(data: ByteArray): ByteArray
    {
        var result: UByteArray = ubyteArrayOf()
        try {
            loadfile(data.toUByteArray())
            initvarcost()
            createMetadata()
            val minbl = createPakdata()
            result = save(p[0], minbl)
        } catch(e: Exception) {
            e.printStackTrace()
        }
        return result.toByteArray()
    }

    private fun createPakdata(): Int {
        var minlen = sourceSize * 1000
        var minbl = 0
        var l: Int
        p = Array(8) { Array(0) { Pakdata() } }
        for (i in 0 until 7) {
            p[i] = Array(sourceSize + 1) { Pakdata() }
            l = getlen(p[i], i)
            if (l < minlen && i != 0) {
                minlen = l
                minbl = i
            }
        }
        return minbl
    }

    private fun loadfile(data: UByteArray)
    {
        sourceSize = data.size
        d = UByteArray(sourceSize + 1) { i -> data.getOrElse(i) { 0u } }
        m = Array(sourceSize + 1) { Metadata() }
    }

    // Inits output buffer
    private fun init()
    {
        s.ep = 0
        s.dp = 0
        s.p = 0
        s.e = 0
        s.buf = UByteArray(sourceSize * 2)
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
    private fun adddata(d: UByte)
    {
        s.buf[s.dp++] = d
    }

    // Dump bits buffer
    private fun addevent()
    {
        s.buf[s.ep] = s.e.toUByte()
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
    private fun done(): UByteArray
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
                varcost[v++] = b.toUInt()
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
            prev[i] = last[d[i].toInt() or (d[i + 1].toInt() shl 8)]
            last[d[i].toInt() or (d[i + 1].toInt() shl 8 )] = i
        }

        // Counts the bytes repeated from each starting position
        r = undefined
        t = 0
        for (i in sourceSize -1 downTo 0) {
            if (d[i] == r.toUByte()) {
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
//TODO optimize while loop
                while (prev[p] != undefined) {
                    p = prev[p]
                    if ((i - p).toUInt() > maxlen[bl]) {   // Exceeded possible offset?
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

        p[sourceSize].cost = 0u

        // Trick: goes from onwards to backwards, this way can know all
        // possible combinations for compressing.
        for (i in sourceSize - 1 downTo 0) {
            kmode = 0   // Literal
            kl = 0
            kc = (9u + p[i + 1].cost).toInt()

            // Test every size until getting the most short
            j = m[i].clen[0]
            while (j > 1) {
                cc = (9u + varcost[j - 1] + p[i + j].cost).toInt()
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
                cc = ccc + (varcost[j - 1] + p[i + j].cost).toInt()
                if (cc < kc) {
                    kc = cc
                    kmode = 2      // Long offset
                    kl = j
                }
                --j
            }
            p[i].cost = kc.toUInt()
            p[i].mode = kmode
            p[i].mlen = kl
        }
        return p[0].cost.toInt()
    }

    // Save the compressed file
    private fun save(p: Array<Pakdata>, mode: Int): UByteArray
    {
        var i = 1
        var j: Int

        init()
        if (savelength != 0) {
            adddata((sourceSize and 255).toUByte())
            adddata((sourceSize shr 8).toUByte())
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
                    adddata(j.toUByte())
                    i += p[i].mlen
                }
                2 -> {      // Long offset
                    add1()
                    addvar(p[i].mlen - 1)
                    j = m[i].cpos[mode] - 1
                    adddata((128 or (j and 127)).toUByte())
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

    /**
        Max input size: 16Kb.
        Max output size: 48Kb.
     */
    override fun uncompress(data: ByteArray): ByteArray {
        return PletterUncompress().uncompress(data)
    }

    class PletterUncompress: AsmZ80Helper()
    {
        fun uncompress(data: ByteArray): ByteArray {
            val startOut = 0x4000
            clearMemory()
            data.copyInto(memory, 0)
            hl.ld(0)                          // Pointer to data (0)
            de.ld(startOut)                         // Pointer to out (startOut)

            a.ld(data[hl.get()])                    // ld a,(hl)
            hl.inc()                                // inc hl
            exx()                                   // exx
            de.ld(0)                                // ld de, 0
            a.add(a)                                // add a, a
            a.inc()                                 // inc a
            e.rl()                                  // rl  e
            a.add(a)                                // add a, a
            e.rl()                                  // rl  e
            a.add(a)                                // add a, a
            e.rl()                                  // rl  e
            e.rl()                                  // rl  e
            hl.ld(0)                                // ld hl, modes
            hl.add(de)                              // add hl, de
            ix.ld(hl.get() / 2)
            e.ld(1)                                 // ld e, 1
            exx()                                   // exx
            // iy.ld(loop)
//literal:
            ldi()                                   // ldi
//loop:
            loop@ while (true) {
                a.add(a)                            // add a, a
                if (zeroFlag) getBit()         // call z,getbit
                if (!carryFlag) {
                    ldi()                           // jr nc,literal
                    continue
                }
                exx()                               // exx
                hl.ld(de)                           // ld h,d | ld l,e
//getlen:
                a.add(a)                            // add a, a
                if (zeroFlag) getBitexx()      // call z,getbitexx
                if (carryFlag) {               // jr nc,.lenok
//.lus:
                    lus@ do {
                        a.add(a)                    // add a,a
                        if (zeroFlag) getBitexx()// call z,getbitexx
                        hl.adc(hl)                  // adc hl, hl
                        if (carryFlag) break@loop  // ret c
                        a.add(a)                    // add a, a
                        if (zeroFlag) getBitexx()// call z,getbitexx
                        if (!carryFlag) break@lus // jr nc,.lenok
                        a.add(a)                    // add a, a
                        if (zeroFlag) getBitexx()// call z,getbitexx
                        hl.adc(hl)                  // adc hl,hl
                        if (carryFlag) break@loop// ret c
                        a.add(a)                    // add a,a
                        if (zeroFlag) getBitexx()// call z,getbitexx
                    } while (carryFlag)        // jp c,.lus
                }
//.lenok:
                hl.inc()                            // inc hl
                exx()                               // exx
                c.ld(data[hl.get()])                // ld c, (hl)
                hl.inc()                            // inc hl
                b.ld(0)                             // ld b, #0
                c.bit(7)                            // bit 7,c
                if (zeroFlag)
                    offsak()                        // jp z,offsok
                else
                    modes[ix.get()]()               // jp (ix)
                // jp (iy)
            }

            de.exx()
            return memory.copyOfRange(startOut, de.get())
        }

        private fun getBit() {
            a.ld(memory[hl.get()])              // ld a,(hl)
            hl.inc()                            // inc hl
            a.rla()                             // rla
        }

        private fun getBitexx() {
            exx()                               // exx
            a.ld(memory[hl.get()])              // ld a,(hl)
            hl.inc()                            // inc hl
            exx()                               // exx
            a.rla()                             // rla
        }

        private fun offsak() {
            bc.inc()                            // inc bc
            push(hl)                   // push hl
            exx()                               // exx
            push(hl)                   // push hl
            exx()                               // exx
            hl.ld(de)                           // ld l,e | ld h,d
            hl.sbc(bc)                          // sbc hl,bc
            pop(bc)                    // pop bc
            ldir()                              // ldir
            pop(hl)                    // pop hl
        }

        private fun mode2() {
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            if (carryFlag) {               // jr nc,offsok
                a.or(a)                         // or a
                b.inc()                         // inc b
                c.res(7)                        // res 7, c
            }
            offsak()
        }

        private fun mode3() {
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode2()
        }

        private fun mode4() {
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode3()
        }

        private fun mode5() {
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode4()
        }

        private fun mode6() {
            a.add(a)                            // add a,a
            if (zeroFlag) getBit()         // call z,getbit
            b.rl()                              // rl b
            mode5()
        }

        private val modes = arrayOf(
            { offsak() }, { mode2() }, { mode3() }, { mode4() }, { mode5() }, { mode6() }
        )
    }
}
