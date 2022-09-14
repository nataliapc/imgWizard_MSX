package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.enums.*
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.*
import java.lang.RuntimeException


interface ScreenMSX : ScreenRectangle, ScreenFullImage {
    fun from(file: File): ScreenMSX
    fun from(stream: InputStream): ScreenMSX
    fun saveTo(file: File, start: Int = 0, end: Int = 0)
    val pixelType: PixelType
    val paletteType: PaletteType
}

interface ScreenMSXCompanion {
    fun from(file: File): ScreenMSX
    fun from(file: File, extension: FileExt): ScreenMSX
    fun from(stream: InputStream, extension: FileExt): ScreenMSX
}

interface ScreenBitmap : ScreenMSX

interface ScreenTiled: ScreenMSX

object ScreenFactory {
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

open class ScreenBitmapImpl(
    val screenMode: ScreenModeType,
    final override val pixelType: PixelType,
    final override val paletteType: PaletteType,
    final val chipset: Chipset = Chipset.V9990,
    val extension: FileExt = FileExt.Unknown
) : ScreenBitmap {

    class SC5 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BP4, PaletteType.GRB333, Chipset.V9938, FileExt.SC5)
    class SC6 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BP2, PaletteType.GRB333, Chipset.V9938, FileExt.SC6)
    class SC7 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BP4, PaletteType.GRB333, Chipset.V9938, FileExt.SC7)
    class SC8 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD8, PaletteType.GRB332, Chipset.V9938, FileExt.SC8)
    class SC10 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYJKP, PaletteType.GRB333, Chipset.V9958, FileExt.SCA)
    class SC12 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYJK, PaletteType.Unspecified, Chipset.V9958, FileExt.SCC)
    class B1withBP4 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BP4, PaletteType.GRB555)
    class B1withBD8 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD8, PaletteType.GRB555)
    class B1withBYUV : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYUV, PaletteType.GRB555)
    class B1withBD16 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD16, PaletteType.GRB555)
    class B3withBD16 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BD16, PaletteType.GRB555)
    class B5withBD4 : ScreenBitmapImpl(ScreenModeType.B5, PixelType.BP4, PaletteType.GRB555)
    class B6withBD4 : ScreenBitmapImpl(ScreenModeType.B6, PixelType.BP4, PaletteType.GRB555)
    class B7withBD4 : ScreenBitmapImpl(ScreenModeType.B7, PixelType.BP4, PaletteType.GRB555)
    class B7iwithBD4 : ScreenBitmapImpl(ScreenModeType.B7_I, PixelType.BP4, PaletteType.GRB555)

    val magicNumber = 0xfe
    val maxVRAMsizeInBytes = chipset.ramKb * 1024
    val screenSizeInBytes: Int = screenMode.width * screenMode.height * pixelType.bpp / 8
    var maxScreenHeight: Int = screenSizeInBytes / screenMode.width
    lateinit var vram: ByteArray

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
                else -> TODO("Not yet implemented")
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
        vram = ByteArray(maxVRAMsizeInBytes)

        val dis = DataInputStream(stream)
        if (dis.readUnsignedByte() != magicNumber) {
            throw RuntimeException("Bad header first byte")
        }
        val startAddress = dis.readUnsignedShortLE()
        dis.readNBytes(4)                            // Skip 4 bytes (end address, execution address)
        dis.readAllBytes().copyInto(vram, startAddress)  // Read full raw VRAM dump

        return this
    }

    override fun saveTo(file: File, start: Int, end: Int) {
        DataByteArrayOutputStream().use {
            it.writeByte(magicNumber)
            it.writeShortLE(start)
            it.writeShortLE(end)
            it.writeShortLE(0)
            it.write(vram, start, end - start)
            file.writeBytes(it.toByteArray())
        }
    }

    override fun getFullImage(): ByteArray {
        return vram.copyOf()
    }

    override fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray {
        return when(this) {
            is SC8 -> {
                val out = ByteArrayOutputStream()
                for (yPos in y until y+h) {
                    for (xPos in x until x+w) {
                        out.write(getPixel(xPos, yPos))
                    }
                }
                out.toByteArray()
            }
            else -> {
                TODO("Not yet implemented")
            }
        }
    }

    override fun getPixel(x: Int, y: Int): Int {
        return when(this) {
            is SC8 -> vram[x + y * screenMode.width].toUByte().toInt()
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
