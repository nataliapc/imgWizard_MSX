package org.nataliapc.imagewizard.utils

import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.RuntimeException

class MSXToRGB24InputStream(stream: InputStream, private val pixelType: PixelType, private val paletteType: PaletteType) : DataByteArrayInputStream(stream)
{
    constructor(byteArray: ByteArray, pixelType: PixelType, paletteType: PaletteType) : this(ByteArrayInputStream(byteArray), pixelType, paletteType)

    fun readColor(): Int {
        return when {
            paletteType.isShortSized() -> paletteType.toRGB24(readUnsignedShort())
            paletteType.isByteSized() -> {
                val value = readUnsignedByte()
                if (pixelType.indexed) value else paletteType.toRGB24(value)
            }
            else -> throw RuntimeException("Unknown palette type to read")
        }
    }

}