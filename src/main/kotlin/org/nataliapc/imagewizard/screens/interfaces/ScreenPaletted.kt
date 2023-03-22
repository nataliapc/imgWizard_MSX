package org.nataliapc.imagewizard.screens.interfaces

import org.nataliapc.imagewizard.screens.PaletteMSX

interface ScreenPaletted
{
    val paletteOffset: Int
    val vram: ByteArray
    fun getPalette(): ByteArray = vram.copyOfRange(paletteOffset, paletteOffset + 32)
    fun setPalette(palette: PaletteMSX) { palette.getRaw().copyInto(vram, paletteOffset) }
}