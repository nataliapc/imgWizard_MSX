package org.nataliapc.imagewizard.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import java.lang.RuntimeException

internal class RGB24ToMSXOutputStreamTest {

    @Test
    fun constructor_Fail() {
        assertThrows(RuntimeException::class.java) {
            RGB24ToMSXOutputStream(PixelType.BP6, PaletteType.Unspecified)
        }
    }

    @Test
    fun writeColor() {
    }

    @Test
    fun writeFlush_BD8_GRB332() {
        val stream = RGB24ToMSXOutputStream(PixelType.BD8, PaletteType.GRB332)
        stream.writeColor(0xff0080)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b00011110), stream.toByteArray())
    }

    @Disabled
    @Test
    fun writeFlush_BD8_GRB555() {
        val stream = RGB24ToMSXOutputStream(PixelType.BD8, PaletteType.GRB555)
        stream.writeColor(0xaa)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b00011110), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP4_1pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP4, PaletteType.GRB333)
        stream.writeColor(0xf)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b11110000.toByte()), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP4_2pixels() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP4, PaletteType.GRB333)
        stream.writeColor(0xf)
        stream.writeColor(0x4)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b11110100.toByte()), stream.toByteArray())
    }

    @Test
    fun resetWrite() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP4, PaletteType.GRB333)
        stream.writeColor(0xf)

        stream.resetWrite()

        assertArrayEquals(byteArrayOf(), stream.toByteArray())
    }
}