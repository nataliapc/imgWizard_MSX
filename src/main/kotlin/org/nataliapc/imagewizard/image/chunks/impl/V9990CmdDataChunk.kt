package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkCreateFrom
import org.nataliapc.imagewizard.image.chunks.ChunkData
import org.nataliapc.imagewizard.screens.interfaces.ScreenRectangle
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk V9990 Send data to Command Port (P#2):
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (33)
        0x0001  2    Extra header length (3)
        0x0003  2    Data length (1-2040)
        --extra header--
        0x0005  1    Compressor ID
        0x0006  2    Uncompressed data length
        ---data---
        0x0008 ...   Compressed data (1-2040 bytes length)
 */
class V9990CmdDataChunk private constructor() : ChunkAbstractImpl(33),
    ChunkData
{
    var compressor = Compressor.Types.byId(0)
    private var compressedData = byteArrayOf()
    private var uncompressedSize: Int = 0

    private constructor(compressor: Compressor): this() {
        this.compressor = compressor
    }

    constructor(data: ByteArray, compressor: Compressor) : this(compressor) {
        compressedData = compressor.compress(data)
        uncompressedSize = data.size
        if (compressedData.size > Chunk.MAX_CHUNK_DATA_SIZE) {
            throw RuntimeException("Maximum Chunk data size exceeded (max:${Chunk.MAX_CHUNK_DATA_SIZE} current:${compressedData.size})")
        }
        if (!compressor.uncompress(compressedData).contentEquals(data)) {
            throw RuntimeException("Error uncompressing data with ${compressor.javaClass.simpleName}")
        }
    }

    companion object : ChunkCreateFrom
    {
        override fun from(stream: DataInputStream): V9990CmdDataChunk {
            val obj = V9990CmdDataChunk()
            obj.readChunk(stream)
            return obj
        }

        fun fromRectangle(scr: ScreenRectangle, x: Int, y: Int, w: Int, h: Int, compressor: Compressor): V9990CmdDataChunk {
            return V9990CmdDataChunk(scr.getRectangle(x, y, w, h), compressor)
        }
    }

    override fun getRawData(): ByteArray = compressedData

    override fun getUncompressedData(): ByteArray = compressor.uncompress(compressedData)

    override fun readExtraHeader(stream: DataInputStream) {
        compressor = Compressor.Types.byId(stream.readUnsignedByte())
        uncompressedSize = stream.readUnsignedShortLE()
    }

    override fun readData(stream: DataInputStream) {
        compressedData = stream.readNBytes(dataLength)
    }

    override fun ensembleExtraHeader(): ByteArray {
        val out = DataByteArrayOutputStream()
        out.use {
            it.writeByte(compressor.id)
            it.writeShortLE(uncompressedSize)
        }
        return out.toByteArray()
    }

    override fun ensembleData(): ByteArray {
        return compressedData
    }

    override fun printInfo() {
        println("V9990 ${compressor.javaClass.simpleName.uppercase()} Data Command: $uncompressedSize bytes (${compressedData.size} bytes compressed) [${compressedData.size*100/uncompressedSize}%]")
    }
}