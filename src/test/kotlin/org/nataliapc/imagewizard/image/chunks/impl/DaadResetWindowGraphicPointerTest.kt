package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.utils.DataByteArrayInputStream

internal class DaadResetWindowGraphicPointerTest {

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(16, 0,0, 50,0))

        val result = DaadResetWindowGraphicPointer.from(stream)

        assertEquals(16, result.getId())
    }

    @Test
    fun build_Ok() {
        val result = DaadResetWindowGraphicPointer().build()

        assertArrayEquals(
            byteArrayOf(16, 0,0, 0,0),
            result
        )
    }

    @Test
    fun printInfo_Ok() {
        val result = DaadResetWindowGraphicPointer()

        result.printInfo()
    }
}