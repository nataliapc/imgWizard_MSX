package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class InfoChunkTest {

    @Test
    fun build_Ok()
    {
        val chunk = InfoChunk()

        val result = chunk.build()

        assertEquals(128, chunk.getId())
        assertArrayEquals(
            byteArrayOf(128u.toByte(), 3,0, 0,0, 1, 0,0),
            result)
    }
}