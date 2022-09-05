package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ArraysTest {

    @Test
    fun byteArray_toHex_Ok() {
        val array = byteArrayOf(0, 1, 2, 3, 4, 5, 255u.toByte())

        val result = array.toHex()

        assertEquals("00 01 02 03 04 05 ff", result)
    }

    @Test
    fun intArray_toHex_Ok() {
        val array = intArrayOf(0xff0102, 0x0102ff, 0x01ff02, 0xffffff)

        val result = array.toHex()

        assertEquals("ff0102 0102ff 01ff02 ffffff", result)
    }
}