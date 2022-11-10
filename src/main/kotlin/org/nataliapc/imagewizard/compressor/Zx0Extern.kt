package org.nataliapc.imagewizard.compressor

import org.nataliapc.imagewizard.utils.AsmZ80Helper
import java.io.File

class Zx0Extern : CompressorImpl(4) {
    override fun compress(data: ByteArray): ByteArray {
        val fileIn = File.createTempFile("zx0In", ".tmp")
        val fileOut = File.createTempFile("zx0Out", ".tmp")
        fileIn.outputStream().use { it.write(data); it.flush() }
        fileOut.delete()

        val proc = Runtime.getRuntime().exec("zx0 ${fileIn.absolutePath} ${fileOut.absolutePath}")
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
        val fileIn = File.createTempFile("zx0In", ".tmp")
        val fileOut = File.createTempFile("zx0Out", ".tmp")
        fileIn.outputStream().use { it.write(data); it.flush() }
        fileOut.delete()

        val proc = Runtime.getRuntime().exec("dzx0 ${fileIn.absolutePath} ${fileOut.absolutePath}")
        //println(BufferedReader(proc.inputStream.reader()).readText())
        proc.waitFor()

        return fileOut.inputStream().use {
            val dataOut = it.readAllBytes()
            fileIn.delete()
            fileOut.delete()

            dataOut
        }
    }
}