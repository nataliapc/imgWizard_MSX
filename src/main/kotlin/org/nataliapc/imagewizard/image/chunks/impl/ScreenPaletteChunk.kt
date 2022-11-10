package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.image.chunks.ChunkLegacy
import org.nataliapc.imagewizard.image.chunks.ChunkPalette
import org.nataliapc.imagewizard.screens.PaletteMSX
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
**** LEGACY CHUNK ****
    Chunk Screen Palette format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (1:palette)
        0x0001  2    The size of the palette (always 32)
        0x0003  2    The size of the palette (always 32)
        ---data---
        0x0005  32   GRB333 palette data in 2 bytes (12 bits) format (0xRB 0x0G)
 */
class ScreenPaletteChunk(val palette: ByteArray) : ChunkAbstractImpl(1), ChunkPalette, ChunkLegacy
{
    private var auxData: Int = 0

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): ScreenPaletteChunk {
            val id = stream.readUnsignedByte()
            val len = stream.readUnsignedShortLE()
            val aux = stream.readUnsignedShortLE()
            val palette = stream.readNBytes(len)

            val obj = ScreenPaletteChunk(palette)
            obj.auxData = aux
            obj.checkId(id)
            obj.checkPalette()
            return obj
        }
    }

    constructor(palette: PaletteMSX): this(palette.getRaw())

    override fun getRawData(): ByteArray = palette

    override fun build(): ByteArray
    {
        checkPalette()
        auxData = palette.size

        val out = DataByteArrayOutputStream()
        out.use {
            it.writeByte(getId())
            it.writeShortLE(auxData)
            it.writeShortLE(auxData)
            it.write(palette)
        }
        return out.toByteArray()
    }

    override fun printInfo() {
        println("Screen Palette")
    }

    private fun checkPalette() {
        if (palette.size != 32) {
            throw RuntimeException("Palette must be 32 bytes length")
        }
    }
}