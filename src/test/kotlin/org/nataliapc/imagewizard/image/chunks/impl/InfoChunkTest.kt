package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class InfoChunkTest {

    @Test
    fun build_Ok()
    {
        val chunk = InfoChunk()
        chunk.originalWidth = 256
        chunk.originalHeight = 212

        val result = chunk.build()

        assertEquals(128, chunk.getId())
        assertArrayEquals(
            byteArrayOf(128u.toByte(), 10,0, 0,0, 1, 0,0, 0,1, 212u.toByte(),0, 0, 0, 0),
            result)
    }

    @Test
    fun printInfo_Ok() {
        val chunk = InfoChunk()

        chunk.printInfo()
    }
}