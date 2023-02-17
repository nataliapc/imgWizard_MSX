package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk.*
import org.nataliapc.utils.DataByteArrayInputStream


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
            byteArrayOf(32, 0,0, 21,0, 1,0, 2,0, 3,0, 4,0, 5,0, 6,0, 7, 0, 9,0, 10,0, 11,0, 0),
            result
        )
    }

    @Test
    fun createFrom_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(
            32, 21,0, 0,0, 1,0,2,0, 3,0,4,0, 5,0,6,0,
            7, LogicalOp.IMP.value.toByte(), 9,0, 10,0, 11,0,
            Command.Stop.value.toByte()
        ))

        val result = V9990CmdChunk.from(stream)

        assertEquals(32, result.getId())
    }

    @Test
    fun printInfo_Ok() {
        val chunk = V9990CmdChunk(
            1,2,3,4,5,6,7, LogicalOp.None,9,10,11, Command.Stop
        )

        chunk.printInfo()
    }

    @Test
    fun instanceStop_Ok() {
        val result = Stop()

        assertEquals(32, result.getId())
    }

    @Test
    fun instanceRectangleToSend_Ok() {
        val result = SendRectangle(0, 0, 100, 200)

        assertEquals(32, result.getId())
    }

    @Test
    fun instanceFill_Ok() {
        val result = Fill(0, 0, 100, 200, 0xffff00)

        assertEquals(32, result.getId())
    }

    @Test
    fun instanceCopy_Ok() {
        val result = Copy(0, 0, 100, 100, 50, 50)

        assertEquals(32, result.getId())
    }
}
