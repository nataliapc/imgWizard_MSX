package org.nataliapc.imagewizard.screens.enums

enum class PixelType(val mask: Int, val indexed: Boolean) {
    Unspecified(0, false),
    BP2(0b11, true),            // 4 colors (2 bpp)
    BP4(0b1111, true),          // 16 colors (4 bpp)
    BP6(0xff, true),            // 64 colors (6 bpp)
    BD8(0xff, false),           // 256 colors (GRB332)
    BD16(0xffff, false),        // 32768 colors (GRB555)
    BYJK(0xff, false),          // 19268 colors (16 bpp)
    BYJKP(0xff, false),         // 12599 colors + 16 paletted colors
    BYUV(0xff, false),          // 19268 colors
    BYUVP(0xff, false);         // 12599 colors + 16 paletted colors

    val bpp = mask.countOneBits()
    val pixelsPerByte = 8.0 / bpp

    companion object {
        fun byId(id: Int): PixelType {
            return values()[id]
        }
    }

    fun isByteSized() = bpp <= 8
    fun isShortSized() = bpp <=16 && !isByteSized()

}