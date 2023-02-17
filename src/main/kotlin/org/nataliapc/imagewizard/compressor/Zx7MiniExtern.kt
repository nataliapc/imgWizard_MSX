package org.nataliapc.imagewizard.compressor

import org.nataliapc.utils.AsmZ80Helper
import java.io.File

class Zx7MiniExtern : CompressorImpl(3) {
    override fun compress(data: ByteArray): ByteArray {
        val fileIn = File.createTempFile("zx7In", ".tmp")
        val fileOut = File.createTempFile("zx7Out", ".tmp")
        fileIn.outputStream().use { it.write(data); it.flush() }
        fileOut.delete()

        val proc = Runtime.getRuntime().exec("zx7mini ${fileIn.absolutePath} ${fileOut.absolutePath}")
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
            hl.ld(startIn)                          // Pointer to data (startIn)
            de.ld(startOut)                         // Pointer to out (startOut)

//dzx7
            a.ld(0x80)                        // ld      a, $80
//copyby
            ldi()                                   // ldi
//mainlo
            mainlo@ do {
                getbit()                            // call    getbit
                if (!carryFlag) {                   //jr      nc, copyby
                    do {
                        ldi()
                        getbit()
                    } while(!carryFlag)
                }
                bc.ld(1)                      // ld      bc, 1
//lenval
                do {
                    getbit()                        // call    getbit
                    c.rl()                          // rl      c
                    if (carryFlag) break@mainlo     // ret     c
                    getbit()                        // call    getbit
                } while(!carryFlag)                 //jr      nc, lenval
                push(hl)                            // push    hl
                l.ld(memory[hl.get()])              // ld      l, (hl)
                h.ld(b)                             // ld      h, b
                push(de)                            // push    de
                exDEHL()                            // ex      de, hl
                hl.sbc(de)                          // sbc     hl, de
                pop(de)                             // pop     de
                ldir()                              // ldir
                pop(hl)                             // pop     hl
                hl.inc()                            // inc     hl
            } while(true)                           //jr      mainlo

            return memory.copyOfRange(startOut, de.get())
        }
        private fun getbit() {
            a.add(a)
            if (!zeroFlag) return                   // ret     nz
            a.ld(memory[hl.get()])                  // ld      a, (hl)
            hl.inc()                                // inc     hl
            a.adc(a)                                // adc     a, a
        }
    }
/*
; -----------------------------------------------------------------------------
; ZX7 mini by Einar Saukas, Antonio Villena
; "Standard" version (43/39 bytes only)
; -----------------------------------------------------------------------------
; Parameters:
;   HL: source address (compressed data)
;   DE: destination address (decompressing)
; -----------------------------------------------------------------------------

dzx7    ld      a, $80
copyby  ldi
mainlo  call    getbit
        jr      nc, copyby
        ld      bc, 1
lenval  call    getbit
        rl      c
        ret     c
        call    getbit
        jr      nc, lenval
        push    hl
        ld      l, (hl)
        ld      h, b
        push    de
        ex      de, hl
        sbc     hl, de
        pop     de
        ldir
        pop     hl
        inc     hl
        jr      mainlo
getbit  add     a, a
        ret     nz
        ld      a, (hl)
        inc     hl
        adc     a, a
        ret
*/
}