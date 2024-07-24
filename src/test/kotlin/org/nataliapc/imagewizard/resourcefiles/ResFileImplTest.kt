package org.nataliapc.imagewizard.resourcefiles

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.compressor.Compressor

class ResFileImplTest {

    private val resName = "noname"
    private val content = byteArrayOf(0,1,2,3,4,5,6,7,8,9)
    private val compressor = Compressor.Types.RAW.instance
    private val resIndex = ResFileImpl.IndexResource(
        resName,
        10,
        compressor,
        content.size,
        content.size,
        0)
    private val resElement = ResElementByteArray(resIndex, content)

    @Test
    fun addResource() {
        val resFile = ResFileImpl()

        resFile.addResource(resElement)
        val result = resFile.getReource(0)

        assertEquals(resElement, result)
    }

    @Test
    fun build() {
        //TODO
    }

    @Test
    fun generateInclude() {
        //TODO
    }

}