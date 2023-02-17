package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.utils.DataByteArrayInputStream
import java.lang.RuntimeException


internal class ScreenPaletteChunkTest {

    private val palette = byteArrayOf(
        0x00, 0x00, 0x00, 0x00, 0x22, 0x05, 0x33, 0x06,
        0x26, 0x02, 0x37, 0x03, 0x52, 0x03, 0x37, 0x06,
        0x62, 0x03, 0x73, 0x04, 0x62, 0x06, 0x65, 0x06,
        0x22, 0x04, 0x55, 0x03, 0x66, 0x06, 0x77, 0x07
    )

    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf(1, 32,0, 32,0) + palette)

        val result = ScreenPaletteChunk.from(stream)

        assertEquals(1, result.getId())
        assertArrayEquals(palette, result.palette)
    }

    @Test
    fun build_Ok() {
        val result = ScreenPaletteChunk(palette).build()

        assertArrayEquals(
            byteArrayOf(1, 32,0, 32,0) + palette,
            result
        )
    }

    @Test
    fun build_Fail() {
        assertThrows(RuntimeException::class.java) {
            ScreenPaletteChunk(byteArrayOf(0, 0, 0, 0, 0, 0)).build()
        }
    }

    @Test
    fun printInfo_Ok() {
        val result = ScreenPaletteChunk(palette)

        result.printInfo()
    }
}