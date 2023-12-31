package org.nataliapc.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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
    fun writeFlush_BD8_GRB332_1pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BD8, PaletteType.GRB332)
        stream.writeColor(0xff0080)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b00011110), stream.toByteArray())
    }

    @Test
    fun writeFlush_BD8_GRB332_2pixels() {
        val stream = RGB24ToMSXOutputStream(PixelType.BD8, PaletteType.GRB332)
        stream.writeColor(0xff0080)
        stream.writeColor(0x0000ff)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b00011110, 0b00000011), stream.toByteArray())
    }

    @Test
    fun writeFlush_BD8_GRB555() {
        val stream = RGB24ToMSXOutputStream(PixelType.BD8, PaletteType.GRB555)
        stream.writeColor(0x1a)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0x1a), stream.toByteArray())
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
    fun writeFlush_BP4_3pixels() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP4, PaletteType.GRB333)
        stream.writeColor(0x4)
        stream.writeColor(0xf)
        stream.writeColor(0x7)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0x4f, 0x70), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP2_1pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP2, PaletteType.GRB333)
        stream.writeColor(0x1)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b01000000), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP2_2pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP2, PaletteType.GRB333)
        stream.writeColor(0x1)
        stream.writeColor(0x2)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b01100000), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP2_3pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP2, PaletteType.GRB333)
        stream.writeColor(0x1)
        stream.writeColor(0x2)
        stream.writeColor(0x3)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b01101100), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP2_4pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP2, PaletteType.GRB333)
        stream.writeColor(0x1)
        stream.writeColor(0x2)
        stream.writeColor(0x3)
        stream.writeColor(0x1)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b01101101), stream.toByteArray())
    }

    @Test
    fun writeFlush_BP2_5pixel() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP2, PaletteType.GRB333)
        stream.writeColor(0x1)
        stream.writeColor(0x2)
        stream.writeColor(0x3)
        stream.writeColor(0x1)
        stream.writeColor(0x1)

        stream.writeFlush()

        assertArrayEquals(byteArrayOf(0b01101101, 0b01000000), stream.toByteArray())
    }

    @Test
    fun resetWrite() {
        val stream = RGB24ToMSXOutputStream(PixelType.BP4, PaletteType.GRB333)
        stream.writeColor(0xf)

        stream.resetWrite()

        assertArrayEquals(byteArrayOf(), stream.toByteArray())
    }
}