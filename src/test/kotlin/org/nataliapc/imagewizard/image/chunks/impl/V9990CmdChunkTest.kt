package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.*


internal class V9990CmdChunkTest {

    @Test
    fun build_Ok()
    {
        val chunk = V9990CmdChunk(
            1,2,3,4,5,6,7, LogicalOp.None,9,10,11, Command.Stop
        )

        val result = chunk.build()

        assertEquals(32, chunk.getId())
        assertArrayEquals(
            byteArrayOf(32, 21,0, 0,0, 1,0, 2,0, 3,0, 4,0, 5,0, 6,0, 7, 0, 9,0, 10,0, 11,0, 0),
            result
        )
    }
}
