package org.nataliapc.imagewizard.image

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.nataliapc.imagewizard.image.chunks.impl.DaadClearWindow
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import java.lang.RuntimeException


internal class ImgXImplTest {

    private val buildValid = "IMGX".toByteArray() +
            byteArrayOf(128u.toByte(), 10,0, 0,0, 1, 1,0, 0,0, 0,0, 0, 0, 0) +
            byteArrayOf(17, 0,0, 0,0)

    private lateinit var imgx: ImgX


    @BeforeEach
    fun setUp() {
        imgx = ImgXImpl()
    }

    @Test
    fun build_Ok() {
        val result = ImgXImpl()
            .add(DaadClearWindow())
            .build()

        assertArrayEquals(buildValid, result)
    }

    @Test
    fun get_Ok() {
        val result = imgx.get(0).getId()

        assertEquals(result, 128)
    }

    @Test
    fun fromStream_Ok() {
        val stream = DataByteArrayInputStream(buildValid)

        val result = ImgXImpl.from(stream)

        assertEquals(2, result.chunkCount())
        assertEquals(128, result.get(0).getId())
        assertEquals(17, result.get(1).getId())
    }

    @Test
    fun fromStream_Fail() {
        val stream = DataByteArrayInputStream("IMXX".toByteArray())

        assertThrows(RuntimeException::class.java) {
            ImgXImpl.from(stream)
        }
    }

    @Test
    fun add() {
        imgx.add(DaadClearWindow())

        assertEquals(2, imgx.chunkCount())
        assertEquals(17, imgx.get(1).getId())
    }

    @Test
    fun addAt() {
        imgx.addAt(0, DaadClearWindow())

        assertEquals(2, imgx.chunkCount())
        assertEquals(17, imgx.get(0).getId())
    }

    @Test
    fun remove() {
        imgx.add(DaadClearWindow())

        imgx.remove(0)

        assertEquals(1, imgx.chunkCount())
        assertEquals(17, imgx.get(0).getId())
    }

    @Test
    fun removeLast() {
        imgx.add(DaadClearWindow())

        imgx.removeLast()

        assertEquals(1, imgx.chunkCount())
        assertEquals(128, imgx.get(0).getId())
    }

    @Test
    fun chunkCount() {
        imgx.add(DaadClearWindow())

        assertEquals(2, imgx.chunkCount())
    }

}