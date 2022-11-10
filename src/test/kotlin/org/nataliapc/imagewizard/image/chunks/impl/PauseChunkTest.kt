package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream


internal class PauseChunkTest {

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(19, 2,0, 0,0, 50,0))

        val result = PauseChunk.from(stream)

        assertEquals(19, result.getId())
        assertEquals(50, result.pauseTicks)
    }

    @Test
    fun getTicks_Ok() {
        val chunk = PauseChunk(50)

        val result = chunk.pauseTicks

        assertEquals(50, result)
    }

    @Test
    fun setTicks_Ok() {
        val chunk = PauseChunk(50)
        chunk.pauseTicks = 25

        assertEquals(25, chunk.pauseTicks)
    }

    @Test
    fun build_Ok() {
        val result = PauseChunk(50).build()

        assertArrayEquals(
            byteArrayOf(19, 2,0, 0,0, 50,0),
            result
        )
    }

    @Test
    fun printInfo_Ok() {
        val result = PauseChunk(500)

        result.printInfo()
    }
}