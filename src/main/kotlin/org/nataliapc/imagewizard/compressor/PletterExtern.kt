package org.nataliapc.imagewizard.compressor

import java.io.BufferedReader
import java.io.File
import java.io.StringReader

class PletterExtern : CompressorImpl(130)
{
    override fun compress(data: ByteArray): ByteArray {
        val fileIn = File.createTempFile("pletterIn", ".tmp")
        val fileOut = File.createTempFile("pletterOut", ".tmp")
        fileIn.outputStream().use { it.write(data) ; it.flush() }
        fileOut.delete()

        val proc = Runtime.getRuntime().exec("pletter ${fileIn.absolutePath} ${fileOut.absolutePath}")
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
        return Pletter().uncompress(data)
    }
}