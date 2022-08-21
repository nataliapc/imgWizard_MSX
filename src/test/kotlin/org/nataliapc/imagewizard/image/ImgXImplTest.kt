package org.nataliapc.imagewizard.image

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ImgXImplTest {

    @Test
    fun get_Ok() {
        val image = ImgXImpl()

        val result = image.get(0).getId()

        assertEquals(result, 128)
    }

    @Test
    fun add() {
    }

    @Test
    fun addAt() {
    }

    @Test
    fun remove() {
    }

    @Test
    fun removeLast() {
    }

    @Test
    fun chunkCount() {
    }

    @Test
    fun build() {
    }
}