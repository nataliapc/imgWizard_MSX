package org.nataliapc.imagewizard.compressor

import org.nataliapc.utils.AsmZ80Helper
import java.io.File

class Zx7Extern : CompressorImpl(3) {
    override fun compress(data: ByteArray): ByteArray {
        val fileIn = File.createTempFile("zx7In", ".tmp")
        val fileOut = File.createTempFile("zx7Out", ".tmp")
        fileIn.outputStream().use { it.write(data); it.flush() }
        fileOut.delete()

        val proc = Runtime.getRuntime().exec("zx7 ${fileIn.absolutePath} ${fileOut.absolutePath}")
        //println(BufferedReader(proc.inputStream.reader()).readText())
        proc.waitFor()

        return fileOut.inputStream().use {
            val dataOut = it.readAllBytes()
            fileIn.delete()
            fileOut.delete()

            dataOut
        }
    }

    override fun uncompress(data: ByteArray): ByteArray {
        return Zx7Uncompress().uncompress(data)
    }

    class Zx7Uncompress : AsmZ80Helper()
    {
        fun uncompress(data: ByteArray): ByteArray {
            val startIn = 0x0000
            val startOut = 0x4000
            clearRegs()
            clearMemory()
            data.copyInto(memory, startIn)
            hl.ld(startIn)                          // Pointer to data (0)
            de.ld(startOut)                         // Pointer to out (startOut)

//dzx7:
            bc.ld(0x8000)                       // marker bit at MSB of B
            a.ld(b)                                 // move marker bit to A
//copyby:
            c.inc()                                 // to keep BC value after LDD
            ldd()                                   // copy literal byte
//mainlo:
            do {
                getbit()                            // read next bit
                if (!carryFlag) {
                    do {
                        c.inc()
                        ldd()
                        getbit()
                    } while (!carryFlag)            // next bit indicates either literal or sequence
                }
                // determine number of bits used for length (Elias gamma coding)
                push(de)                            // store destination on stack
                d.ld(c)                             // D= 0, assume BC=$0000 here (or $8000)
//lenval:
                do {
                    if (!carryFlag) getbit()        // don't call first time (to save bytes)
                    c.rl()
                    b.rl()                          // insert bit on BC
                    getbit()                        // more bits ?
                } while (!carryFlag)                // repeat, final length on BC

                // check escape sequence
                c.inc()                             // detect escape sequence
                //jr z, exitdz
                if (!zeroFlag) {                    // end of algorithm
                    // determine offset
                    e.ld(memory[hl.get()])          // load offset flag (1 bit) + offset value (7 bits)
                    hl.dec()
                    e.sll()
                    //jr nc, offend
                    if (carryFlag) {                // offset flag is set, load 4 extra bits
                        d.ld(0x10)            // bit marker to load 4 bits
//nexbit:
                        do {
                            getbit()
                            d.rl()                  // insert next bit into D
                        } while (!carryFlag)        // repeat 4 times, until bit marker is out
                        d.inc()                     // add 128 to DE
                        d.srl()                     // retrieve fourth bit from D
                    }
//offend:
                    e.rr()                          // insert fourth bit into E
                    exSPHL()                        // store source, restore destination
                    exDEHL()                        // destination from HL to DE
                    hl.adc(de)                      // HL = destination + offset + 1
                    lddr()
                }
//exitdz:
                pop(hl)                             // restore source address (compressed data )
            } while (!carryFlag)
            //jr nc, mainlo                         // end of loop. exit with getbit instead ret (-1 byte)
            getbit()

            return memory.copyOfRange(startOut, de.get())
        }

        private fun getbit() {
            a.add(a)                                // check next bit
            if (!zeroFlag) return                   // no more bits left ?
            a.ld(memory[hl.get()])                  // load another group of 8 bits
            hl.dec()
            a.adc(a)
        }
    }
}
/*
; Parameters:
;   HL: source address (compressed data)
;   DE: destination address (decompressing)
; -----------------------------------------------------------------------------
dzx7:   ld      bc, $8000       ; marker bit at MSB of B
        ld      a, b            ; move marker bit to A
copyby: inc     c               ; to keep BC value after LDD
        ldd                     ; copy literal byte
mainlo: call    getbit          ; read next bit
        jr      nc, copyby      ; next bit indicates either literal or sequence

; determine number of bits used for length (Elias gamma coding)
        push    de              ; store destination on stack
        ld      d, c            ; D= 0, assume BC=$0000 here (or $8000)
lenval: call    nc, getbit      ; don't call first time (to save bytes)
        rl      c
        rl      b               ; insert bit on BC
        call    getbit          ; more bits?
        jr      nc, lenval      ; repeat, final length on BC

; check escape sequence
        inc     c               ; detect escape sequence
        jr      z, exitdz       ; end of algorithm

; determine offset
        ld      e, (hl)         ; load offset flag (1 bit) + offset value (7 bits)
        dec     hl
        sll     e
        jr      nc, offend      ; if offset flag is set, load 4 extra bits
        ld      d, $10          ; bit marker to load 4 bits
nexbit: call    getbit
        rl      d               ; insert next bit into D
        jr      nc, nexbit      ; repeat 4 times, until bit marker is out
        inc     d               ; add 128 to DE
        srl     d               ; retrieve fourth bit from D
offend: rr      e               ; insert fourth bit into E
        ex      (sp), hl        ; store source, restore destination
        ex      de, hl          ; destination from HL to DE
        adc     hl, de          ; HL = destination + offset + 1
        lddr
exitdz: pop     hl              ; restore source address (compressed data)
        jr      nc, mainlo      ; end of loop. exit with getbit instead ret (-1 byte)

getbit: add     a, a            ; check next bit
        ret     nz              ; no more bits left?
        ld      a, (hl)         ; load another group of 8 bits
        dec     hl
        adc     a, a
        ret
*/
