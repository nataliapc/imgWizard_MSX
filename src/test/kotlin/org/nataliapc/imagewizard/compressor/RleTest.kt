package org.nataliapc.imagewizard.compressor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RleTest {

    private val rle = Rle()
    private val dataIn = "12234445666666666677776866966660".toByteArray()


    @Test
    fun getId_Ok() {
        val result = rle.id

        assertEquals(1, result)
    }

    @Test
    fun compress_Ok() {
        val result = rle.compress(dataIn)

        assertArrayEquals(
            "\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
        assertArrayEquals(dataIn, rle.uncompress(result))
    }

    @Test
    fun compress_withSize_Ok() {
        val result = Rle(addSize = true).compress(dataIn)

        assertArrayEquals(
            "\u0020\u0000\u000012234445\u0000\u000a6\u0000\u0004768669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
        assertArrayEquals(dataIn, Rle(addSize = true).uncompress(result))
    }

    @Test
    fun compress_withMark_Ok() {
        val result = Rle(mark = '7'.code.toByte()).compress(dataIn)

        print(result.toList())
        assertArrayEquals(
            "7122344457\u000a67\u00047686697\u0004607\u0000".toByteArray(),
            result
        )
        assertArrayEquals(dataIn, rle.uncompress(result))
    }

    @Test
    fun compress_transparent_Ok() {
        val result = Rle(transparent = '7'.code.toByte()).compress(dataIn)

        print(result.toList())
        assertArrayEquals(
            "\u000012234445\u0000\u000a6\u0000\u0002\u000468669\u0000\u000460\u0000\u0000".toByteArray(),
            result
        )
        assertArrayEquals(
            dataIn.map { if (it=='7'.code.toByte()) 0 else it }.toByteArray(),
            rle.uncompress(result)
        )
    }
}