package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.writeShortLE
import java.awt.Color
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.lang.RuntimeException
import kotlin.math.round


interface ScreenMSX : ScreenRectangle, ScreenFullImage {
    fun fromFile(file: File): ScreenMSX
    val pixelType: PixelType
    val paletteType: PaletteType
}

interface ScreenBitmap : ScreenMSX

interface ScreenTiled: ScreenMSX

class ScreenFactory {
    fun getSC5(): ScreenMSX = ScreenBitmapImpl.SC5()
    fun getSC6(): ScreenMSX = ScreenBitmapImpl.SC6()
    fun getSC7(): ScreenMSX = ScreenBitmapImpl.SC7()
    fun getSC8(): ScreenMSX = ScreenBitmapImpl.SC8()
    fun getSC10(): ScreenMSX = ScreenBitmapImpl.SC10()
    fun getSC12(): ScreenMSX = ScreenBitmapImpl.SC12()
    fun readSC5(file: File): ScreenMSX = ScreenBitmapImpl.SC5().fromFile(file)
    fun readSC6(file: File): ScreenMSX = ScreenBitmapImpl.SC6().fromFile(file)
    fun readSC7(file: File): ScreenMSX = ScreenBitmapImpl.SC7().fromFile(file)
    fun readSC8(file: File): ScreenMSX = ScreenBitmapImpl.SC8().fromFile(file)
    fun readSC10(file: File): ScreenMSX = ScreenBitmapImpl.SC10().fromFile(file)
    fun reafSC12(file: File): ScreenMSX = ScreenBitmapImpl.SC12().fromFile(file)
}

enum class Chipset(val ramKb: Int) {
    Unspecified(0),
    TMS9918(16),
    V9938(128),
    V9958(128),
    V9990(512);

    companion object {
        fun byId(id: Int): Chipset {
            return values()[id]
        }
    }
}

enum class Interlaced {
    None,
    Enabled
}

enum class Signal {
    Single,
    PAL,
    NTSC
}

enum class ScreenModeType(
    val mode: Int,
    val width: Int,
    val height: Int,
    val interlaced: Interlaced = Interlaced.None,
    val signal: Signal = Signal.Single,
    val overscan: Boolean = false
) {
    B0_NTSC(0,192,240, signal = Signal.NTSC, overscan = true),              // V9990 B0 NTSC
    B0_PAL(0,192,290, signal = Signal.PAL, overscan = true),                // V9990 B0 PAL
    B0_NTSC_I(0,192,240, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B0 NTSC Interlaced
    B0_PAL_I(0,192,290, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B0 PAL Interlaced
    B1(1,256,212),                                                          // SC5i, SC8i, SC10i, SC12i, V9990 B1
    B1_I(1,256,424, Interlaced.Enabled),                                    // SC5i, SC8i, SC10i, SC12i, V9990 B1 Interlaced
    B2_NTSC(2,384,240, signal = Signal.NTSC, overscan = true),              // V9990 B2 NTSC
    B2_PAL(2,384,290, signal = Signal.PAL, overscan = true),                // V9990 B2 PAL
    B2_NTSC_I(2,384,480, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B2 NTSC Interlaced
    B2_PAL_I(2,384,580, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B2 PAL Interlaced
    B3(3,512,212),                                                          // SC6, SC7, V9990 B3
    B3_I(3,512,424, Interlaced.Enabled),                                    // SC6i, SC7i, V9990 B3 Interlaced
    B4_NTSC(4,768,240, signal = Signal.NTSC, overscan = true),              // V9990 B4 NTSC
    B4_PAL(4,768,290, signal = Signal.PAL, overscan = true),                // V9990 B4 PAL
    B4_NTSC_I(4,768,480, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B4 NTSC Interlaced
    B4_PAL_I(4,768,580, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B4 PAL Interlaced
    B5(5,640,400),                                                          // V9990 B5
    B6(6,640,480),                                                          // V9990 B6
    B7(7,1024,212),                                                         // V9990 B7
    B7_I(7,1024,424, Interlaced.Enabled)                                    // V9990 B7 Interlaced
}

enum class PixelType(val mask: Int, val indexed: Boolean) {
    Unspecified(0, false),
    BP2(0b11, true),            // 4 colors (2 bpp)
    BP4(0b1111, true),          // 16 colors (4 bpp)
    BP6(0b111111, true),        // 64 colors (6 bpp)
    BD8(0xff, false),           // 256 colors (GRB332)
    BD16(0xffff, false),        // 32768 colors (GRB555)
    BYJK(0xff, false),          // 19268 colors (16 bpp)
    BYJKP(0xff, false),         // 12599 colors + 16 paletted colors
    BYUV(0xff, false),          // 19268 colors
    BYUVP(0xff, false);         // 12599 colors + 16 paletted colors

    val bpp = mask.countOneBits()

    companion object {
        fun byId(id: Int): PixelType {
            return values()[id]
        }
    }
}

enum class PaletteType(val bpp: Int, private val rMask: Int, private val gMask: Int, private val bMask: Int) {
    Unspecified(0, 0,0,0),
    GRB332(8, 0b00011100,0b11100000,0b00000011),
    GRB333(11, 0b00001110000,0b11100000000,0b00000000111),
    GRB555(15, 0b0000001111100000,0b0111110000000000,0b0000000000011111);

    private val rIni: Int = rMask.countTrailingZeroBits()
    private val gIni: Int = gMask.countTrailingZeroBits()
    private val bIni: Int = bMask.countTrailingZeroBits()
    private val rMax: Int = rMask shr rIni
    private val gMax: Int = gMask shr gIni
    private val bMax: Int = bMask shr bIni

    companion object {
        fun byId(id: Int): PaletteType {
            return values()[id]
        }
    }

    fun isByteSized() = bpp <= 8
    fun isShortSized() = bpp <=16 && !isByteSized()

    fun fromRGB24(rgb: Int): Int {
        val color = Color(rgb)
        return fromRGB24(color.red, color.green, color.blue)
    }

    fun fromRGB24(red: Int, green: Int, blue: Int): Int {
        rMask.countLeadingZeroBits()
        val r = round(red * rMax / 255.0).toInt() shl rIni
        val g = round(green * gMax / 255.0).toInt() shl gIni
        val b = round(blue * bMax / 255.0).toInt() shl bIni
        return r or g or b
    }

    fun toRGB24(value: Int): Int {
        val r = round(((value and rMask) shr rIni) * 255.0 / rMax).toInt() shl 16
        val g = round(((value and gMask) shr gIni) * 255.0 / gMax).toInt() shl 8
        val b = round(((value and bMask) shr bIni) * 255.0 / bMax).toInt()
        return r or g or b
    }

    fun writeFromRGB24(rgb: Int, stream: DataOutputStream, pixelType: PixelType) {
        when {
            isShortSized() -> stream.writeShortLE(fromRGB24(rgb))
            isByteSized() -> stream.writeByte(if (pixelType.indexed) rgb else fromRGB24(rgb))
            else -> throw RuntimeException("Unknown palette type to write")
        }
    }

    fun readToRGB24(stream: DataInputStream, pixelType: PixelType): Int {
        return when {
            isShortSized() -> toRGB24(stream.readUnsignedShort())
            isByteSized() -> {
                val value = stream.readUnsignedByte()
                if (pixelType.indexed) value else toRGB24(value)
            }
            else -> throw RuntimeException("Unknown palette type to read")
        }
    }
}

open class ScreenBitmapImpl(
    val screenMode: ScreenModeType,
    final override val pixelType: PixelType,
    final override val paletteType: PaletteType,
    final val chipset: Chipset = Chipset.V9990
) : ScreenBitmap {

    class SC5 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BP4, PaletteType.GRB333, Chipset.V9938)
    class SC6 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BP2, PaletteType.GRB333, Chipset.V9938)
    class SC7 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BP4, PaletteType.GRB333, Chipset.V9938)
    class SC8 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD8, PaletteType.GRB332, Chipset.V9938)
    class SC10 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYJKP, PaletteType.GRB333, Chipset.V9958)
    class SC12 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYJK, PaletteType.Unspecified, Chipset.V9958)
    class B1withBP4 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BP4, PaletteType.GRB555)
    class B1withBD8 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD8, PaletteType.GRB555)
    class B1withBYUV : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BYUV, PaletteType.GRB555)
    class B1withBD16 : ScreenBitmapImpl(ScreenModeType.B1, PixelType.BD16, PaletteType.GRB555)
    class B3withBD16 : ScreenBitmapImpl(ScreenModeType.B3, PixelType.BD16, PaletteType.GRB555)
    class B5withBD4 : ScreenBitmapImpl(ScreenModeType.B5, PixelType.BP4, PaletteType.GRB555)
    class B6withBD4 : ScreenBitmapImpl(ScreenModeType.B6, PixelType.BP4, PaletteType.GRB555)
    class B7withBD4 : ScreenBitmapImpl(ScreenModeType.B7, PixelType.BP4, PaletteType.GRB555)
    class B7iwithBD4 : ScreenBitmapImpl(ScreenModeType.B7_I, PixelType.BP4, PaletteType.GRB555)

    val maxVRAMsizeInBytes = chipset.ramKb * 1024
    val screenSizeInBytes: Int = screenMode.width * screenMode.height * pixelType.bpp / 8
    var maxScreenHeight: Int = screenSizeInBytes / screenMode.width

    init {
        if (screenMode.mode >= 4 && pixelType != PixelType.BP4 && pixelType != PixelType.BP2) {
            throw RuntimeException("Invalid Screen format: Not supported ${pixelType.bpp}bpp for mode B${screenMode.mode}")
        }
        if (screenSizeInBytes > maxVRAMsizeInBytes) {
            throw RuntimeException("Invalid Screen format: This mode oversized (${screenSizeInBytes/1024} Kb) max VRAM capacity (${maxVRAMsizeInBytes/1024} Kb)")
        }
        calculateMaxScreenHeight()
    }

    companion object {
        fun from(file: File): ScreenMSX {
            TODO("Not yet implemented")
        }
    }

    override fun fromFile(file: File): ScreenMSX {
        TODO("Not yet implemented")
    }

    override fun getFullImage(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun getRectangle(x: Int, y: Int, w: Int, h: Int): ByteArray {
        TODO("Not yet implemented")
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
