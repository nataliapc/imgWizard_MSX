package org.nataliapc.imagewizard.screens.enums

enum class PixelType(val mask: Int, val colors: Int, val indexed: Boolean) {
    Unspecified(0, 0, false),
    BP2(0b11, 4,  true),            // 4 colors (2 bpp)
    BP4(0b1111, 16, true),          // 16 colors (4 bpp)
    BP6(0b111111, 64,  true),       // 64 colors (6 bpp)
    BD8(0xff, 256, false),          // 256 colors (GRB332)
    BD16(0xffff, 32768, false),     // 32768 colors (GRB555)
    BYJK(0xff, 19268, false),       // 19268 colors (16 bpp)
    BYJKP(0xff, 12499, false),      // 12499 colors + 16 paletted colors
    BYUV(0xff, 19268, false),       // 19268 colors
    BYUVP(0xff, 12499, false);      // 12499 colors + 16 paletted colors

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