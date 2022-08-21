package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class V9990CmdDataChunkTest {

    @Test
    fun build_Ok() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5))

        val result = chunk.build()

        Assertions.assertArrayEquals(
            byteArrayOf(33, 5,0, 5,0, 1,2,3,4,5),
            result
        )
    }
}