package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.utils.DataByteArrayInputStream
import java.lang.RuntimeException


internal class DaadSkipBytesTest {

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(18, 2,0, 0,0, 50,0))

        val result = DaadSkipBytes.from(stream)

        assertEquals(18, result.getId())
        assertEquals(50, result.skipBytes)
    }

    @Test
    fun getSkipBytes_Ok() {
        val chunk = DaadSkipBytes(50)

        val result = chunk.skipBytes

        assertEquals(50, result)
    }

    @Test
    fun setSkipBytes_Ok() {
        val chunk = DaadSkipBytes(50)
        chunk.skipBytes = 25

        assertEquals(25, chunk.skipBytes)
    }

    @Test
    fun setSkipBytes_Fail() {
        DaadSkipBytes(0)
        DaadSkipBytes(65535)

        assertThrows(RuntimeException::class.java) {
            DaadSkipBytes(-1)
        }
        assertThrows(RuntimeException::class.java) {
            DaadSkipBytes(65536)
        }
    }

    @Test
    fun build_Ok() {
        val result = DaadSkipBytes(50).build()

        assertArrayEquals(
            byteArrayOf(18, 2,0, 0,0, 50,0),
            result
        )
    }

    @Test
    fun printInfo_Ok() {
        val result = DaadSkipBytes(500)

        result.printInfo()
    }
}