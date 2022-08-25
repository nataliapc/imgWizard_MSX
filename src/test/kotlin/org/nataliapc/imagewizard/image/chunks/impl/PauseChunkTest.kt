package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import toHex


internal class PauseChunkTest {

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(19, 0,0, 50,0))

        val result = PauseChunk.from(stream)

        assertEquals(19, result.getId())
        assertEquals(50, result.getTicks())
    }

    @Test
    fun getTicks_Ok() {
        val chunk = PauseChunk(50)

        val result = chunk.getTicks();

        assertEquals(50, result)
    }

    @Test
    fun setTicks_Ok() {
        val chunk = PauseChunk(50)
        chunk.setTicks(25);

        assertEquals(25, chunk.getTicks())
    }

    @Test
    fun build_Ok() {
        val result = PauseChunk(50).build()

        assertArrayEquals(
            byteArrayOf(19, 0,0, 50,0),
            result
        )
    }

    @Test
    fun printInfo_Ok() {
        val result = PauseChunk(500)

        result.printInfo()
    }
}