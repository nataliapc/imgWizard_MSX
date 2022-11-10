package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import java.lang.RuntimeException

internal class DaadRedirectToImageTest {

    @Test
    fun location_Ok()
    {
        val chunk = DaadRedirectToImage(63)

        val result = chunk.location

        assertEquals(63, result)
    }

    @Test
    fun setLocation_Fail()
    {
        DaadRedirectToImage(0)
        DaadRedirectToImage(255)

        assertThrows(RuntimeException::class.java) {
            DaadRedirectToImage(-1)
        }
        assertThrows(RuntimeException::class.java) {
            DaadRedirectToImage(256)
        }
    }

    @Test
    fun createFrom_Ok() {
        val result = DaadRedirectToImage.from(
            DataByteArrayInputStream(byteArrayOf(0, 1,0, 0,0, 7)))

        assertEquals(0, result.getId())
        assertEquals(7, result.location)
    }

    @Test
    fun build_Ok()
    {
        val chunk = DaadRedirectToImage(63)

        val result = chunk.build()

        assertArrayEquals(
            byteArrayOf(0, 1,0, 0,0, 63),
            result)
    }

    @Test
    fun printInfo_Ok() {
        val chunk = DaadRedirectToImage(63)

        chunk.printInfo()
    }
}