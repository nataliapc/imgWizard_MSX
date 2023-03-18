package org.nataliapc.imagewizard.resourcefiles

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.compressor.Compressor

class ResFileImplTest {

    private val resName = "noname"
    private val content = byteArrayOf(0,1,2,3,4,5,6,7,8,9)
    private val resElement = ResElementByteArray(resName, content)
    private val compressor = Compressor.Types.RAW.instance

    @Test
    fun addResource() {
        val resFile = ResFileImpl(compressor)

        resFile.addResource(resElement)
        val result = resFile.

        assertEquals()
    }

    @Test
    fun build() {
        //TODO
    }

    @Test
    fun generateInclude() {
        //TODO
    }

    @Test
    fun getCompressor() {
        val resFile = ResFileImpl(compressor)

        val result = resFile.compressor

        assertEquals(compressor, result)
    }
}