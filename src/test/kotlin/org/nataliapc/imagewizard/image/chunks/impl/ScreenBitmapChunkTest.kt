package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.nataliapc.imagewizard.compressor.Pletter
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.imagewizard.compressor.Rle
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.image.chunks.impl.ScreenBitmapChunk.Companion.ID_BASE
import kotlin.test.assertEquals


internal class ScreenBitmapChunkTest {

    private val raw = Raw()
    private val rle = Rle()
    private val pletter = Pletter()


    @Test
    fun from_Ok() {
        val stream = DataByteArrayInputStream(byteArrayOf((ID_BASE+raw.id).toByte(), 4,0, 4,0, 0,0,0,0))

        val result = ScreenBitmapChunk.from(stream)

        assertEquals(ID_BASE+raw.id, result.getId())
        assertArrayEquals(byteArrayOf(0,0,0,0), result.getRawData())
        assertArrayEquals(byteArrayOf(0,0,0,0), result.getUncompressedData())
    }

    @Test
    fun build_Ok() {
        val chunk = ScreenBitmapChunk(byteArrayOf(0,0,0,0), raw)

        val result = chunk.build()

        assertArrayEquals(
            byteArrayOf((ID_BASE+raw.id).toByte(), 4,0, 4,0, 0,0,0,0),
            result
        )
    }

    @Disabled
    @Test
    fun fromFullImage_Ok() {

    }

    @Disabled
    @Test
    fun fromRectangle_Ok() {

    }

    @Test
    fun getCompressor_Ok() {
        val chunk = ScreenBitmapChunk(byteArrayOf(0,0,0,0), pletter)

        val result = chunk.compressor

        assertEquals(pletter.id, result.id)
    }

    @Test
    fun getRawData_Ok() {
        val chunk = ScreenBitmapChunk(byteArrayOf(0,0,0,0), rle)

        val result = chunk.getRawData()

        assertArrayEquals(
            byteArrayOf(1,1,4,0,1,0),
            result
        )
    }

    @Test
    fun getUncompressedData_Ok() {
        val chunk = ScreenBitmapChunk(byteArrayOf(0,0,0,0), pletter)

        val result = chunk.getUncompressedData()

        assertArrayEquals(
            byteArrayOf(0,0,0,0),
            result
        )
    }

    @Test
    fun printInfo_Ok() {
        val result = ScreenBitmapChunk(byteArrayOf(0,0,0,0), Raw())

        result.printInfo()
    }
}