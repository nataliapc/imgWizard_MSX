package org.nataliapc.imagewizard.compressor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class RawTest {

    private val dataIn = "12234445666666666677776866966660".toByteArray()


    @Test
    fun getId_Ok() {
        val result = Raw().id

        assertEquals(0, result)
    }

    @Test
    fun compress_Ok() {
        val result = Raw().compress(dataIn)

        assertArrayEquals(dataIn, result)
    }
}