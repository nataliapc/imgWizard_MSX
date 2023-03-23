package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.interfaces.ScreenPaletted
import org.nataliapc.utils.DataByteArrayInputStream
import org.nataliapc.utils.readUnsignedShortLE
import java.lang.RuntimeException


interface PaletteMSX {
    companion object {
        val defaultMSX1 = byteArrayOf(0x00,0x00, 0x00,0x00, 0x12,0x05, 0x33,0x06, 0x27,0x02, 0x37,0x03, 0x62,0x02, 0x27,0x06,
                                      0x72,0x02, 0x73,0x03, 0x62,0x05, 0x64,0x06, 0x11,0x04, 0x65,0x02, 0x66,0x06, 0x77,0x07)
    }

    object Factory {
        fun from(paletteRaw: ByteArray, paletteType: PaletteType): PaletteMSX = PaletteMSXImpl(paletteRaw, paletteType)
    }

    fun setRaw(newPalette: ByteArray)
    fun getRaw(): ByteArray
    fun getOriginalColor(index: Int): Int
    fun getColorRGB24(index: Int): Int
}

private class PaletteMSXImpl(private var paletteType: PaletteType) : PaletteMSX {
    private var paletteRaw: ByteArray = ByteArray(32)
    private lateinit var originalColorIntArray: IntArray
    private lateinit var colorRGB24IntArray: IntArray

    constructor(paletteRaw: ByteArray, paletteType: PaletteType) : this(paletteType) {
        setRaw(paletteRaw)
    }

    constructor(screenMSX: ScreenMSX): this((screenMSX as ScreenPaletted).getPalette(), screenMSX.paletteType)

    override fun setRaw(newPalette: ByteArray) {
        if (paletteRaw.size != 32) {
            throw RuntimeException("Palette must be of 32 bytes")
        }
        paletteRaw = newPalette
        createInternalArrays()
    }

    override fun getRaw(): ByteArray {
        return paletteRaw
    }

    override fun getOriginalColor(index: Int): Int {
        return originalColorIntArray[index]
    }

    override fun getColorRGB24(index: Int): Int {
        return colorRGB24IntArray[index]
    }

    private fun createInternalArrays() {
        val colorOrigList = mutableListOf<Int>()
        val color24List = mutableListOf<Int>()
        DataByteArrayInputStream(paletteRaw).use {
            for (index in 0..15) {
                val originalColor = it.readUnsignedShortLE()
                colorOrigList.add(originalColor)
                color24List.add(paletteType.toRGB24(originalColor))
            }
        }
        originalColorIntArray = colorOrigList.toIntArray()
        colorRGB24IntArray = color24List.toIntArray()
    }
}