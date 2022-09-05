package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DataByteArrayOutputStreamTest {

    @Test
    fun toByteArray_Ok() {
        val stream = DataByteArrayOutputStream()
        stream.writeByte(0x01)
        stream.writeShort(0x0203)
        stream.writeShortLE(0x0504)
        stream.close()

        val result = stream.toByteArray()

        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05), result)
    }
}