package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.enums.*
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenPaletted
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.*
import kotlin.RuntimeException


interface ScreenMSX : ScreenRectangle, ScreenFullImage {
    var vram: ByteArray
    val defaultVRAMsizeInBytes: Int

    fun from(file: File): ScreenMSX
    fun from(stream: InputStream): ScreenMSX
    fun saveTo(file: File, start: Int = 0, end: Int = 0)
    val pixelType: PixelType
    val paletteType: PaletteType
    val chipset: Chipset
    val extension: FileExt
}

interface ScreenMSXCompanion {
    fun from(file: File): ScreenMSX
    fun from(file: File, extension: FileExt): ScreenMSX
    fun from(stream: InputStream, extension: FileExt): ScreenMSX
}

interface ScreenBitmap : ScreenMSX {
    object Factory {
        fun from(file: File): ScreenMSX = ScreenBitmapImpl.from(file)
        fun from(file: File, extension: FileExt): ScreenMSX = ScreenBitmapImpl.from(file, extension)
        fun from(stream: InputStream, extension: FileExt): ScreenMSX = ScreenBitmapImpl.from(stream, extension)
        fun getSC5(): ScreenMSX = ScreenBitmapImpl.SC5()
        fun getSC6(): ScreenMSX = ScreenBitmapImpl.SC6()
        fun getSC7(): ScreenMSX = ScreenBitmapImpl.SC7()
        fun getSC8(): ScreenMSX = ScreenBitmapImpl.SC8()
        fun getSC10(): ScreenMSX = ScreenBitmapImpl.SC10()
        fun getSC12(): ScreenMSX = ScreenBitmapImpl.SC12()
        fun readSC5(file: File): ScreenMSX = ScreenBitmapImpl.SC5().from(file)
        fun readSC6(file: File): ScreenMSX = ScreenBitmapImpl.SC6().from(file)
        fun readSC7(file: File): ScreenMSX = ScreenBitmapImpl.SC7().from(file)
        fun readSC8(file: File): ScreenMSX = ScreenBitmapImpl.SC8().from(file)
        fun readSC10(file: File): ScreenMSX = ScreenBitmapImpl.SC10().from(file)
        fun reafSC12(file: File): ScreenMSX = ScreenBitmapImpl.SC12().from(file)
    }
}

interface ScreenTiled: ScreenMSX

internal sealed class ScreenBitmapImpl(
    val screenMode: ScreenModeType,
    final override val defaultVRAMsizeInBytes: Int,
    final override val pixelType: PixelType,
    final override val paletteType: PaletteType,
    final override val chipset: Chipset = Chipset.V9990,
    override val extension: FileExt = FileExt.Unknown
) : ScreenBitmap
{
    val magicNumber = 0xfe
    val maxVRAMsizeInBytes = chipset.ramKb * 1024
    val screenSizeInBytes: Int = screenMode.width * screenMode.height * pixelType.bpp / 8
    var maxScreenHeight: Int = screenSizeInBytes / screenMode.width
    override var vram: ByteArray = ByteArray(maxVRAMsizeInBytes) { 0 }

    class SC5 : ScreenBitmapImpl(ScreenModeType.B1, 30368, PixelType.BP4, PaletteType.GRB333, Chipset.V9938, FileExt.SC5), ScreenPaletted {
        override val paletteOffset = 0x7680
    }
    class SC6 : ScreenBitmapImpl(ScreenModeType.B3, 30368, PixelType.BP2, PaletteType.GRB333, Chipset.V9938, FileExt.SC6), ScreenPaletted {
        override val paletteOffset = 0x7680
    }
    class SC7 : ScreenBitmapImpl(ScreenModeType.B3, 64160, PixelType.BP4, PaletteType.GRB333, Chipset.V9938, FileExt.SC7), ScreenPaletted {
        override val paletteOffset = 0xfa80
    }
    class SC8 : ScreenBitmapImpl(ScreenModeType.B1, 54272, PixelType.BD8, PaletteType.GRB332, Chipset.V9938, FileExt.SC8)
    class SC10 : ScreenBitmapImpl(ScreenModeType.B1, 64160, PixelType.BYJKP, PaletteType.GRB333, Chipset.V9958, FileExt.SCA), ScreenPaletted {
        override val paletteOffset = 0xfa80
    }
    class SC12 : ScreenBitmapImpl(ScreenModeType.B1, 64160, PixelType.BYJK, PaletteType.Unspecified, Chipset.V9958, FileExt.SCC)
    class B1withBP4 : ScreenBitmapImpl(ScreenModeType.B1, 0, PixelType.BP4, PaletteType.GRB555)
    class B1withBD8 : ScreenBitmapImpl(ScreenModeType.B1, 0, PixelType.BD8, PaletteType.GRB555)
    class B1withBYUV : ScreenBitmapImpl(ScreenModeType.B1, 0, PixelType.BYUV, PaletteType.GRB555)
    class B1withBD16 : ScreenBitmapImpl(ScreenModeType.B1, 0, PixelType.BD16, PaletteType.GRB555)
    class B3withBD16 : ScreenBitmapImpl(ScreenModeType.B3, 0, PixelType.BD16, PaletteType.GRB555)
    class B5withBD4 : ScreenBitmapImpl(ScreenModeType.B5, 0, PixelType.BP4, PaletteType.GRB555)
    class B6withBD4 : ScreenBitmapImpl(ScreenModeType.B6, 0, PixelType.BP4, PaletteType.GRB555)
    class B7withBD4 : ScreenBitmapImpl(ScreenModeType.B7, 0, PixelType.BP4, PaletteType.GRB555)
    class B7iwithBD4 : ScreenBitmapImpl(ScreenModeType.B7_I, 0, PixelType.BP4, PaletteType.GRB555)

    init {
        if (screenMode.mode >= 4 && pixelType != PixelType.BP4 && pixelType != PixelType.BP2) {
            throw RuntimeException("Invalid Screen format: Not supported ${pixelType.bpp}bpp for mode B${screenMode.mode}")
        }
        if (screenSizeInBytes > maxVRAMsizeInBytes) {
            throw RuntimeException("Invalid Screen format: This mode oversized (${screenSizeInBytes/1024} Kb) max VRAM capacity (${maxVRAMsizeInBytes/1024} Kb)")
        }
        calculateMaxScreenHeight()
    }

    companion object : ScreenMSXCompanion
    {
        override fun from(file: File): ScreenMSX {
            val extension: FileExt = FileExt.byFileExtension(file.extension.uppercase())
            return from(file.inputStream(), extension)
        }

        override fun from(file: File, extension: FileExt): ScreenMSX {
            return from(file.inputStream(), extension)
        }

        override fun from(stream: InputStream, extension: FileExt): ScreenMSX {
            return when(extension) {
                FileExt.SC5 -> SC5().from(stream)
                FileExt.SC6 -> SC6().from(stream)
                FileExt.SC7 -> SC7().from(stream)
                FileExt.SC8 -> SC8().from(stream)
                FileExt.SCA -> SC10().from(stream)
                FileExt.SCC -> SC12().from(stream)
                else -> throw RuntimeException("Not supported image type '${extension.name}'")
            }
        }
    }

    override val width: Int
        get() = screenMode.width

    override val height: Int
        get() = screenMode.height

    override fun from(file: File): ScreenMSX {
        return from(DataInputStream(file.inputStream()))
    }

    override fun from(stream: InputStream): ScreenMSX {
        val dis = DataInputStream(stream)
        if (dis.readUnsignedByte() != magicNumber) {
            throw RuntimeException("Bad header first byte")
        }
        val startAddress = dis.readUnsignedShortLE()
        dis.skip(4)                                     // Skip 4 bytes (end address, execution address)
        dis.readAllBytes().copyInto(vram, startAddress)  // Read full raw VRAM dump

        return this
    }

    override fun saveTo(file: File, start: Int, end: Int) {
        val finalEnd = if (end == 0) { defaultVRAMsizeInBytes } else { end }
        DataByteArrayOutputStream().use {
            it.writeByte(magicNumber)
            it.writeShortLE(start)
            it.writeShortLE(finalEnd)
            it.writeShortLE(0)
            it.write(vram, start, finalEnd - start)
            file.writeBytes(it.toByteArray())
        }
    }

    override fun getFullImage(): ByteArray {
        return vram.copyOf()
    }

    override fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray {
        val out = DataByteArrayOutputStream()
        when(this) {
            is SC8 -> {
                for (yPos in y until y+h) {
                    for (xPos in x until x+w) {
                        out.write(getFullBytesFor(xPos, yPos))
                    }
                }
            }
            is SC5, is SC7 -> {
                if (x % 2 != 0 || w % 2 !=0) {
                    throw RuntimeException("Coordenate X and Width must be multiples of 2")
                }
                for (yPos in y until y+h) {
                    for (xPos in x until x+w step 2) {
                        out.write(getFullBytesFor(xPos, yPos))
                    }
                }
            }
            is SC6 -> {
                if (x % 4 != 0 || w % 4 !=0) {
                    throw RuntimeException("Coordenate X and Width must be multiples of 4")
                }
                for (yPos in y until y+h) {
                    for (xPos in x until x+w step 4) {
                        out.write(getFullBytesFor(xPos, yPos))
                    }
                }
            }
            is SC10, is SC12 -> {
                if (x % 4 != 0 || w % 4 !=0) {
                    throw RuntimeException("Coordenate X and Width must be multiples of 4")
                }
                for (yPos in y until y+h) {
                    for (xPos in x until x+w step 4) {
                        out.writeInt(getFullBytesFor(xPos, yPos))
                    }
                }
            }
            else -> { throw RuntimeException("Not a MSX image") }
        }
        return out.toByteArray()
    }

    override fun getPixel(x: Int, y: Int): Int {
        return when(this) {
            is SC8 -> getFullBytesFor(x, y)
            is SC5, is SC7 -> {
                val value = getFullBytesFor(x, y)
                return when (x % 2) {
                    0 -> value.shr(4).and(0x0f)
                    else -> value.and(0x0f)
                }
            }
            is SC6 -> {
                val value = getFullBytesFor(x, y)
                return when (x % 4) {
                    0 -> value.shr(6).and(0x03)
                    1 -> value.shr(4).and(0x03)
                    2 -> value.shr(2).and(0x03)
                    else -> value.and(0x03)
                }
            }
            else -> TODO("Not yet implemented")
        }
    }

    override fun getFullBytesFor(x: Int, y: Int): Int {
        return when(this) {
            is SC8 -> vram[x + y * screenMode.width].toUByte().toInt()
            is SC5, is SC7 -> vram[(x + y * screenMode.width)/2].toUByte().toInt()
            is SC6 -> vram[(x + y * screenMode.width)/4].toUByte().toInt()
            is SC10, is SC12 -> {
                val x0 = x - x % 4
                vram[x0 + y * screenMode.width].toUByte().toInt().shl(24)
                    .or(vram[x0 + y * screenMode.width + 1].toUByte().toInt().shl(16))
                    .or(vram[x0 + y * screenMode.width + 2].toUByte().toInt().shl(8))
                    .or(vram[x0 + y * screenMode.width + 3].toUByte().toInt())
            }
            else -> TODO("Not yet implemented")
        }
    }

    private fun calculateMaxScreenHeight() {
        maxScreenHeight = screenSizeInBytes / screenMode.width
        var pot = (Int.MAX_VALUE.countLeadingZeroBits() - maxScreenHeight.countLeadingZeroBits()).toDouble()
        while (maxScreenHeight * screenMode.width * pixelType.bpp / 8 <= maxVRAMsizeInBytes) {
            pot++
            maxScreenHeight = Math.pow(2.0, pot).toInt()
        }
        pot--
        maxScreenHeight = Math.pow(2.0, pot).toInt()
    }

    override fun toString(): String {
        return "Screen width:${screenMode.width} height:${screenMode.height} (max:$maxScreenHeight) ${pixelType.bpp}bpp ($screenSizeInBytes bytes) "
    }
}
