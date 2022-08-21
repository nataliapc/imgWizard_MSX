package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.nataliapc.imagewizard.compressor.Pletter
import org.nataliapc.imagewizard.compressor.Rle
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl.Companion.MAX_CHUNK_DATA_SIZE
import java.lang.RuntimeException
import kotlin.jvm.Throws

internal class V9990CmdDataChunkTest {

    @Test
    fun build_Ok_Raw() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5), Raw())

        val result = chunk.build()

        assertEquals(33, chunk.getId())
        assertArrayEquals(
            byteArrayOf(33, 5,0, 5,0, 1,2,3,4,5),
            result
        )
    }

    @Test
    fun build_Ok_RLE() {
        val chunk = V9990CmdDataChunk("12234445666666666677776866966660".toByteArray(), Rle())

        val result = chunk.build()

        assertEquals(34, chunk.getId())
        assertArrayEquals(
            byteArrayOf(34, 26,0, 32,0) +
            "\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
    }

    @Test
    fun build_Ok_Pletter() {
        val chunk = V9990CmdDataChunk(byteArrayOf(1,2,3,4,5,1,2,3,4,5,6,7), Pletter())

        val result = chunk.build()

        assertEquals(35, chunk.getId())
        assertArrayEquals(
            byteArrayOf(35, 15,0, 12,0, 1,1,2,3,4,5,-95,4,6,7,-1,-1,-1,-1,-128),
            result
        )
    }

    @Test
    fun build_Fail_ChunkSizeExceeded() {
        assertThrows(RuntimeException::class.java) {
            V9990CmdDataChunk(ByteArray(MAX_CHUNK_DATA_SIZE + 1), Raw())
        }
    }
}