package org.nataliapc.imagewizard.resourcefiles

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedConstruction
import java.io.File
import java.io.FileInputStream

class ResElementFileTest {

    private lateinit var tempFile: File
    private val resName = "noname"
    private val content = byteArrayOf(0,1,2,3,4,5,6,7,8,9)


    @BeforeEach
    fun before() {
        tempFile = File(resName)
        tempFile.delete()
        tempFile.writeBytes(content)
        tempFile.deleteOnExit()
    }

    @Test
    fun getName() {
        val resElement = ResElementFile(tempFile)

        val result = resElement.getName()

        assertEquals(resName, result)
    }

    @Test
    fun getContent() {
        val resElement = ResElementFile(tempFile)

        val result = resElement.getContent()

        assertArrayEquals(content, result)
    }

    @Test
    fun getSize() {
        val resElement = ResElementFile(tempFile)

        val result = resElement.getSize()

        assertEquals(content.size, result)
    }
}