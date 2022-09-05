package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

internal class DataOutputStreamTest {

    @Test
    fun writeByte() {
        val baos = ByteArrayOutputStream()
        val stream = DataOutputStream(baos)

        stream.writeByte(0x70)

        assertArrayEquals(byteArrayOf(0x70), baos.toByteArray())
    }

    @Test
    fun writeShortLE_short() {
        val baos = ByteArrayOutputStream()
        val stream = DataOutputStream(baos)

        stream.writeShortLE(0xff70.toShort())

        assertArrayEquals(byteArrayOf(0x70,0xffu.toByte()), baos.toByteArray())
    }

    @Test
    fun writeShortLE_ushort() {
        val baos = ByteArrayOutputStream()
        val stream = DataOutputStream(baos)

        stream.writeShortLE(0xff70.toUShort())

        assertArrayEquals(byteArrayOf(0x70,0xffu.toByte()), baos.toByteArray())
    }

    @Test
    fun writeShortLE_int() {
        val baos = ByteArrayOutputStream()
        val stream = DataOutputStream(baos)

        stream.writeShortLE(0xff70)

        assertArrayEquals(byteArrayOf(0x70,0xffu.toByte()), baos.toByteArray())
    }
}