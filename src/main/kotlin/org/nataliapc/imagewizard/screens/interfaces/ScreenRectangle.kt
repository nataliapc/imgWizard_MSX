package org.nataliapc.imagewizard.screens.interfaces

interface ScreenRectangle
{
    val width: Int
    val height: Int

    fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray
    fun getPixel(x: Int, y: Int): Int
    fun getFullBytesFor(x: Int, y: Int): Int
}
