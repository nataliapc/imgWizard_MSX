package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.imagewizard.image.chunks.impl.*
import org.nataliapc.imagewizard.utils.DataByteArrayInputStream
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


interface Chunk
{
    class Factory {
        companion object {
            fun createFromStream(stream: DataInputStream): Chunk {
                val id = stream.readUnsignedByte()
                val len = stream.readUnsignedShortLE()
                val buffer = DataByteArrayOutputStream()
                buffer.writeByte(id)
                buffer.writeShortLE(len)
                buffer.writeShortLE(stream.readUnsignedShortLE())
                buffer.write(stream.readNBytes(len))
                val chunkStream = DataByteArrayInputStream(buffer.toByteArray())

                return when (id) {
                    0 -> DaadRedirectToImage.from(chunkStream)
                    1 -> ScreenPaletteChunk.from(chunkStream)
                    2 -> ScreenBitmapChunk.from(chunkStream)
                    3 -> ScreenBitmapChunk.from(chunkStream)
                    4 -> ScreenBitmapChunk.from(chunkStream)
                    16 -> DaadResetWindowGraphicPointer.from(chunkStream)
                    17 -> DaadClearWindow.from(chunkStream)
                    18 -> DaadSkipBytes.from(chunkStream)
                    19 -> PauseChunk.from(chunkStream)
                    32 -> V9990CmdChunk.from(chunkStream)
                    33 -> V9990CmdDataChunk.from(chunkStream)
                    34 -> V9990CmdDataChunk.from(chunkStream)
                    35 -> V9990CmdDataChunk.from(chunkStream)
                    128 -> InfoChunk.from(chunkStream)
                    else -> throw RuntimeException("Unknown Chunk type $id")
                }
            }
        }
    }

    fun getId(): Int
    fun setAuxData(value: Int): Chunk
    fun build(): ByteArray
    fun printInfo()
}

interface ChunkCompanion
{
    fun from(stream: DataInputStream): Chunk
}