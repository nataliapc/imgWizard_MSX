package org.nataliapc.imagewizard.compressor

import java.io.BufferedReader
import java.io.File
import java.io.StringReader

class Zx7Extern : CompressorImpl(3)
{
    override fun compress(data: ByteArray): ByteArray {
        val fileIn = File.createTempFile("zx7In", ".tmp")
        val fileOut = File.createTempFile("zx7Out", ".tmp")
        fileIn.outputStream().use { it.write(data) ; it.flush() }
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
        //TODO
        return data
    }
}