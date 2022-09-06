package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import java.lang.RuntimeException


class RGB24ToMSXOutputStream(private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayOutputStream()
{
    private val byteBits = 8

    private var lastBitWrited = byteBits
    private var currentByte: Int = 0

    init {
        resetWrite()
        if (pixelType.bpp !in intArrayOf(2,4,8,16)) {
            throw RuntimeException("ColorType ${pixelType.bpp}bpp no supported")
        }
    }

    fun writeColor(rgb24: Int) {
        when {
            pixelType.isShortSized() -> writeShortLE(paletteType.toColorMSX(rgb24))
            pixelType.isByteSized() -> {
                if (pixelType.indexed || isIndexedBD8()) {
                    packIndexed(rgb24)
                } else {
                    writeByte(paletteType.toColorMSX(rgb24))
                }
            }
            else -> throw RuntimeException("Unexpected write Color ($rgb24) for ${paletteType.name} ${pixelType.name}")
        }
    }

    fun writeFlush() {
        if (lastBitWrited != byteBits) {
            writeByte(currentByte)
        }
        resetWrite()
    }

    fun resetWrite() {
        currentByte = 0
        lastBitWrited = byteBits
    }

    override fun close() {
        writeFlush()
        super.close()
    }

    private fun packIndexed(value: Int) {
        lastBitWrited -= pixelType.bpp
        currentByte = currentByte or ((value and pixelType.mask) shl lastBitWrited)
        if (lastBitWrited == 0) {
            writeByte(currentByte)
            resetWrite()
        }
    }

    private fun isIndexedBD8(): Boolean = pixelType == PixelType.BD8 && paletteType == PaletteType.GRB555
}