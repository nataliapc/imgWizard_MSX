package org.nataliapc.imagewizard.compressor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RLETest {

    private val dataIn = "12234445666666666677776866966660".toByteArray()


    @Test
    fun getId_Ok() {
        val result = RLE().id

        assertEquals(1, result)
    }

    @Test
    fun compress_Ok() {
        val result = RLE().compress(dataIn)

        assertArrayEquals(
            "\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
    }

    @Test
    fun compress_withSize_Ok() {
        val result = RLE(addSize = true).compress(dataIn)

        assertArrayEquals(
            "\u0020\u0000\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
    }

    @Test
    fun compress_withMark_Ok() {
        val result = RLE(mark = '7'.code.toByte()).compress(dataIn)

        print(result.toList())
        assertArrayEquals(
            "7122344457\u000a67\u00047686697\u0004607\u0000".toByteArray(),
            result
        )
    }

    @Test
    fun compress_transparent_Ok() {
        val result = RLE(transparent = '7'.code.toByte()).compress(dataIn)

        print(result.toList())
        assertArrayEquals(
            "\u000012234445\u0000\u000a6\u0000\u0002\u000468669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
    }
}