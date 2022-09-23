package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.ImgX
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk Info format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (128)
        0x0001  2    Chunk data length (avoiding header)
        0x0003  2    Empty chunk header (filled with 0x00)
        --data--
        0x0005  1    InfoChunk version
        0x0006  2    Chunk count (without info chunk)
 */
class InfoChunk : ChunkAbstractImpl(128)
{
    var infoVersion = 1
        private set
    var chunkCount = 0
        private set
    var originalWidth = 0
    var originalHeight = 0
    var pixelType = PixelType.Unspecified
    var paletteType = PaletteType.Unspecified
    var chipset = Chipset.Unspecified

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): Chunk {
            val id = stream.readUnsignedByte()
            stream.readUnsignedShortLE()

            val obj = InfoChunk()
            obj.checkId(id)

            obj.auxData = stream.readUnsignedShortLE()
            obj.infoVersion = stream.readUnsignedByte()
            obj.chunkCount = stream.readUnsignedShortLE()
            obj.originalWidth = stream.readUnsignedShortLE()
            obj.originalHeight = stream.readUnsignedShortLE()
            obj.pixelType = PixelType.byId(stream.readUnsignedByte())
            obj.paletteType = PaletteType.byId(stream.readUnsignedByte())
            obj.chipset = Chipset.byId(stream.readUnsignedByte())

            return obj
        }

        fun fromMagic(header: String): InfoChunk {
            val obj = InfoChunk()
            obj.let {
                it.originalHeight = 212
                it.chipset = Chipset.V9938
                when (header.substring(3)) {
                    "5" -> { it.originalWidth = 256; it.pixelType = PixelType.BP4; it.paletteType = PaletteType.GRB333 }
                    "6" -> { it.originalWidth = 512; it.pixelType = PixelType.BP2; it.paletteType = PaletteType.GRB333 }
                    "7" -> { it.originalWidth = 512; it.pixelType = PixelType.BP4; it.paletteType = PaletteType.GRB333 }
                    "8" -> { it.originalWidth = 256; it.pixelType = PixelType.BD8; it.paletteType = PaletteType.GRB332 }
                    "A" -> { it.originalWidth = 256; it.pixelType = PixelType.BYJKP; it.paletteType = PaletteType.Unspecified }
                    "C" -> { it.originalWidth = 256; it.pixelType = PixelType.BYJK; it.paletteType = PaletteType.Unspecified }
                    else -> {}
                }
            }
            return obj
        }
    }

    fun update(image: ImgX): Chunk
    {
        chunkCount = image.chunkCount() - 1
        return this
    }

    override fun build(): ByteArray
    {
        val out = DataByteArrayOutputStream()

        out.writeByte(infoVersion)
        out.writeShortLE(chunkCount)
        out.writeShortLE(originalWidth)
        out.writeShortLE(originalHeight)
        out.writeByte(pixelType.ordinal)
        out.writeByte(paletteType.ordinal)
        out.writeByte(chipset.ordinal)

        return ensemble(out.toByteArray())
    }

    override fun getInfo(): Array<String> {
        return arrayOf(
            "Image Info v$infoVersion",
            "        Chunk count ...... $chunkCount",
            "        Original Width ... $originalWidth",
            "        Original Height .. $originalHeight",
            "        Pixel Type ....... ${pixelType.name}",
            "        Palette Type ..... ${paletteType.name}",
            "        Chipset Type ..... ${chipset.name}"
        )
    }
}