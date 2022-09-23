package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import java.lang.RuntimeException


interface PaletteMSX {
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