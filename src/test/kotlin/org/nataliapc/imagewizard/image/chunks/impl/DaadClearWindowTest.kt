package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DaadClearWindowTest {

    @Test
    fun build_Ok()
    {
        val chunk = DaadClearWindow()

        val result = chunk.build()

        assertArrayEquals(
            byteArrayOf(17, 0,0, 0,0),
            result)
    }

    @Test
    fun printInfo_Ok() {
        val chunk = DaadClearWindow()

        chunk.printInfo()
    }
}