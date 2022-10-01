package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.image.chunks.ChunkData
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.readUnsignedShortLE
import org.nataliapc.imagewizard.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk V9990 Send data to Command Port (P#2):
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (Raw:33 RLE:34 Pletter:35)
        0x0001  2    Compressed data length (max: 2043 bytes)
        0x0003  2    Uncompressed data length in bytes
        ---data---
        0x0005 ...   Compressed data (1-2043 bytes length)
 */
class V9990CmdDataChunk private constructor(val compressor: Compressor) : ChunkAbstractImpl(33),
    ChunkData
{
    private var compressedData = byteArrayOf()

    constructor(data: ByteArray, compressor: Compressor) : this(compressor) {
        compressedData = compressor.compress(data)
        auxData = data.size
        if (compressedData.size > MAX_CHUNK_DATA_SIZE) {
            throw RuntimeException("Maximum Chunk data size exceeded (max:$MAX_CHUNK_DATA_SIZE current:${compressedData.size})")
        }
        if (!compressor.uncompress(compressedData).contentEquals(data)) {
            throw RuntimeException("Error uncompressing data with ${compressor.javaClass.simpleName}")
        }
    }

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): V9990CmdDataChunk {
            val id = stream.readUnsignedByte()
            val len = stream.readUnsignedShortLE()
            val auxData = stream.readUnsignedShortLE()
            val compressor = Compressor.Types.byId(stream.read())
            val compressedData = stream.readNBytes(len - 1)

            val obj = V9990CmdDataChunk(compressor)
            obj.auxData = auxData
            obj.compressedData = compressedData
            obj.checkId(id)

            return obj
        }

        fun fromRectangle(scr: ScreenRectangle, x: Int, y: Int, w: Int, h: Int, compressor: Compressor): V9990CmdDataChunk
        {
            return V9990CmdDataChunk(scr.getRectangle(x, y, w, h), compressor)
        }
    }

    override fun getRawData(): ByteArray = compressedData

    override fun getUncompressedData(): ByteArray = compressor.uncompress(compressedData)

    override fun build(): ByteArray
    {
        val out = DataByteArrayOutputStream()

        val header = buildHeader()

        out.write(header)
        out.writeShortLE(compressedData.size + 1)
        out.writeShortLE(auxData)

        out.writeByte(compressor.id)
        out.write(compressedData)

        return out.toByteArray()
    }

    override fun printInfo() {
        println("V9990 ${compressor.javaClass.simpleName.uppercase()} Data Command: $auxData bytes (${compressedData.size} bytes compressed) [${compressedData.size*100/auxData}%]")
    }
}