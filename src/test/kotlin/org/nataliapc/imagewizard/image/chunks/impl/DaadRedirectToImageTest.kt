package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream

internal class DaadRedirectToImageTest {

    @Test
    fun getLocation_Ok()
    {
        val chunk = DaadRedirectToImage(63)

        val result = chunk.location

        assertEquals(63, result)
    }

    @Test
    fun createFrom_Ok() {
        val result = DaadRedirectToImage.from(
            DataByteArrayInputStream(byteArrayOf(0, 0,0, 7,0)))

        assertEquals(0, result.getId())
        assertEquals(7, result.location)
    }

    @Test
    fun build_Ok()
    {
        val chunk = DaadRedirectToImage(63)

        val result = chunk.build()

        assertArrayEquals(
            byteArrayOf(0, 0,0, 63,0),
            result)
    }

    @Test
    fun printInfo_Ok() {
        val chunk = DaadRedirectToImage(63)

        chunk.printInfo()
    }
}