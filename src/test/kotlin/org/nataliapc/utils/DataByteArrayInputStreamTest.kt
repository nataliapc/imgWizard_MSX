package org.nataliapc.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream

internal class DataByteArrayInputStreamTest {

    @Test
    fun constructorStream_Ok() {
        val stream = ByteArrayInputStream(byteArrayOf(1,2,3,4))

        val result = DataByteArrayInputStream(stream)

        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04), result.readAllBytes())
    }

    @Test
    fun constructorByteArray_Ok() {
        val inputData = byteArrayOf(1,2,3,4)

        val result = DataByteArrayInputStream(inputData)

        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04), result.readAllBytes())
    }

}