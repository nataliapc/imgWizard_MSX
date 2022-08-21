package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DaadRedirectToImageTest {

    @Test
    fun getLocation_Ok()
    {
        val chunk = DaadRedirectToImage(63)

        val result = chunk.location

        assertEquals(63, result)
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
}