package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.ImgX
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.screens.Chipset
import org.nataliapc.imagewizard.screens.PixelType
import org.nataliapc.imagewizard.screens.PaletteType
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
    private var infoVersion = 1
    private var chunkCount = 0
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

    override fun printInfo() {
        println("[${getId()}] Image Info v$infoVersion\n"+
                "        Chunk count ...... $chunkCount\n"+
                "        Original Width ... $originalWidth\n"+
                "        Original Height .. $originalHeight\n"+
                "        Pixel Type ....... ${pixelType.name}\n"+
                "        Palette Type ..... ${paletteType.name}\n"+
                "        Chipset Type ..... ${chipset.name}")
    }
}