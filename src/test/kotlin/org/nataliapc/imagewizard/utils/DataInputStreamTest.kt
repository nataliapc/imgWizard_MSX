package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.DataInputStream

internal class DataInputStreamTest {

    @Test
    fun readShortLE_Ok() {
        val stream = DataInputStream(ByteArrayInputStream(byteArrayOf(0x01, 0xffu.toByte())))

        val result = stream.readShortLE()

        assertEquals(0xff01.toShort(), result)
    }

    @Test
    fun readUnsignedShortLE_Ok() {
        val stream = DataInputStream(ByteArrayInputStream(byteArrayOf(0x01, 0xffu.toByte())))

        val result = stream.readUnsignedShortLE()

        assertEquals(0xff01, result)
    }
}