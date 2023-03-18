package org.nataliapc.imagewizard.image.chunks.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.utils.toHex
import kotlin.test.expect

internal class InfoChunkTest {

    @Test
    fun build_Ok()
    {
        val chunk = InfoChunk()
        chunk.originalWidth = 256
        chunk.originalHeight = 212

        val result = chunk.build()

        assertEquals(128, chunk.getId())
        assertArrayEquals(
            byteArrayOf(128u.toByte(), 10,0, 0,0, 1, 0,0, 0,1, 212u.toByte(),0, 0, 0, 0),
            result)
    }

    @Test
    fun printInfo_Ok() {
        val chunk = InfoChunk()

        chunk.printInfo()
    }

    @Test
    fun fromMagic_Ok() {
        val magics = arrayOf("IMG5", "IMG6", "IMG7", "IMG8", "IMGA", "IMGC", "BAD!")
        val widths = arrayOf(256, 512, 512, 256, 256, 256, 0)
        val pixels = arrayOf(PixelType.BP4, PixelType.BP2, PixelType.BP4, PixelType.BD8, PixelType.BYJKP, PixelType.BYJK, PixelType.Unspecified)
        val palettes = arrayOf(PaletteType.GRB333, PaletteType.GRB333, PaletteType.GRB333, PaletteType.GRB332, PaletteType.GRB333, PaletteType.Unspecified, PaletteType.Unspecified)

        for (i in 0..6) {
            val chunk = InfoChunk.fromMagic(magics[i])
            if (chunk != null) {
                val result = chunk.build()
                val expected = byteArrayOf(128.toByte(),    //Id number
                    10,0,                                   //Extra header size
                    0,0,                                    //Data size
                    0,                                      //Info version
                    0,0,                                    //Chunk count
                    0,(widths[i] shr 8).toByte(),           //Original width
                    (if (i!=6) 212 else 0).toByte(),0,      //Original height
                    pixels[i].id().toByte(),                //Pixel type
                    palettes[i].id().toByte(),              //Palette type
                    (if (i!=6) Chipset.V9938.id() else Chipset.Unspecified.id()).toByte()   //Chipset
                )
                assertArrayEquals(expected, result, "Error in screen ${i+5}: ${result.toHex()}")
            } else {
                assertEquals(6, i, "Null only for bad/unknown magic")
            }
        }
    }
}