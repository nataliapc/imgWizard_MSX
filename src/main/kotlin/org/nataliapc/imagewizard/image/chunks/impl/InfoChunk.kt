package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.ImgX
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCreateFrom
import org.nataliapc.imagewizard.screens.enums.Chipset
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.DataInputStream


/*
    Chunk Info format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (128)
        0x0001  2    Extra header length (10)
        0x0003  2    Data length (0)
        --extra header--
        0x0005  1    Info version
        0x0006  2    Chunk count
        0x0008  2    Original width
        0x000a  2    Original height
        0x000c  1    Pixel type
        0x000d  1    Palette type
        0x000e  1    Chipset type
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


    companion object : ChunkCreateFrom {
        override fun from(stream: DataInputStream): Chunk {
            val obj = InfoChunk()
            obj.readChunk(stream)
            return obj
        }

        fun fromMagic(magicHeader: String): InfoChunk? {
            val obj = InfoChunk()
            obj.let {
                it.infoVersion = 0
                it.originalHeight = 212
                it.chipset = Chipset.V9938
                when (magicHeader.substring(3)) {
                    "5" -> { it.originalWidth = 256; it.pixelType = PixelType.BP4; it.paletteType = PaletteType.GRB333 }
                    "6" -> { it.originalWidth = 512; it.pixelType = PixelType.BP2; it.paletteType = PaletteType.GRB333 }
                    "7" -> { it.originalWidth = 512; it.pixelType = PixelType.BP4; it.paletteType = PaletteType.GRB333 }
                    "8" -> { it.originalWidth = 256; it.pixelType = PixelType.BD8; it.paletteType = PaletteType.GRB332 }
                    "A" -> { it.originalWidth = 256; it.pixelType = PixelType.BYJKP; it.paletteType = PaletteType.GRB333 }
                    "C" -> { it.originalWidth = 256; it.pixelType = PixelType.BYJK; it.paletteType = PaletteType.Unspecified }
                    else -> {
                        return null
                    }
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

    override fun readExtraHeader(stream: DataInputStream) {
        infoVersion = stream.readUnsignedByte()
        chunkCount = stream.readUnsignedShortLE()
        originalWidth = stream.readUnsignedShortLE()
        originalHeight = stream.readUnsignedShortLE()
        pixelType = PixelType.byId(stream.readUnsignedByte())
        paletteType = PaletteType.byId(stream.readUnsignedByte())
        chipset = Chipset.byId(stream.readUnsignedByte())
    }

    override fun ensembleExtraHeader(): ByteArray {
        val out = DataByteArrayOutputStream()
        out.use {
            out.writeByte(infoVersion)
            out.writeShortLE(chunkCount)
            out.writeShortLE(originalWidth)
            out.writeShortLE(originalHeight)
            out.writeByte(pixelType.ordinal)
            out.writeByte(paletteType.ordinal)
            out.writeByte(chipset.ordinal)
        }
        return out.toByteArray()
    }

    override fun getInfo(): Array<String> {
        val versionDescription = if (infoVersion == 0) {
            "not found (legacy defaults)"
        } else {
            "v$infoVersion"
        }
        return arrayOf(
            "Image Info $versionDescription",
            "        Chunk count ...... $chunkCount",
            "        Original Width ... $originalWidth",
            "        Original Height .. $originalHeight",
            "        Pixel Type ....... ${pixelType.name}",
            "        Palette Type ..... ${paletteType.name}",
            "        Chipset Type ..... ${chipset.name}"
        )
    }
}