package org.nataliapc.imagewizard.resourcefiles

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.compressor.Compressor

class ResElementByteArrayTest {

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

    @Test
    fun getName() {
        val resElement = ResElementByteArray(resIndex, content)

        val result = resElement.getName()

        assertEquals(resName, result)
    }

    @Test
    fun getContent() {
        val resElement = ResElementByteArray(resIndex, content)

        val result = resElement.getContent()

        assertArrayEquals(content, result)
    }

    @Test
    fun getSize() {
        val resElement = ResElementByteArray(resIndex, content)

        val result = resElement.getSize()

        assertEquals(content.size, result)
    }

    @Test
    fun getCompressedSize() {
        val resElement = ResElementByteArray(resIndex, content)

        val result = resElement.getSize()

        assertEquals(content.size, result)
    }
}