package org.nataliapc.imagewizard.screens

import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.ScreenModeType
import org.nataliapc.imagewizard.screens.interfaces.ScreenFullImage
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import java.io.File
import java.lang.RuntimeException


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
