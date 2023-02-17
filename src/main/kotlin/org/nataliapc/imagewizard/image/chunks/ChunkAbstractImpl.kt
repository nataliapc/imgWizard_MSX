package org.nataliapc.imagewizard.image.chunks

import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.readUnsignedShortLE
import org.nataliapc.utils.writeShortLE
import java.io.DataInputStream
import java.lang.RuntimeException


/*
    Chunk format:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type: (17:ClearWindow)
        0x0001  2    Extra header length
        0x0003  2    Data length (0x0000)
        --extra header--
        ...
        --data--
        ...
 */
abstract class ChunkAbstractImpl(private val id: Int): Chunk
{
    private var extraHeaderLength: Int = 0
    protected var dataLength: Int = 0

    companion object {
        const val MAX_CHUNK_DATA_SIZE = 2043
    }

    final override fun getId(): Int {
        return id
    }

    override fun printInfo() {
        getInfo().forEach {
            println(it)
        }
    }

    final override fun printInfoWithOrdinal(ordinal: Int) {
        print("CHUNK #$ordinal [id:${getId()}] ")
        printInfo()
    }

    override fun build(): ByteArray {
        return ensemble(ensembleExtraHeader(), ensembleData())
    }

    protected open fun ensembleExtraHeader(): ByteArray = byteArrayOf()
    protected open fun ensembleData(): ByteArray = byteArrayOf()

    override fun getInfo(): Array<String> {
        return arrayOf("**** UNKNOWN CHUNK TYPE ****")
    }

    protected fun readChunk(stream: DataInputStream) {
        readHeader(stream)
        readExtraHeader(stream)
        readData(stream)
    }

    private fun readHeader(stream: DataInputStream) {
        checkId(stream.readUnsignedByte())
        extraHeaderLength = stream.readUnsignedShortLE()
        dataLength = stream.readUnsignedShortLE()
    }

    protected open fun readExtraHeader(stream: DataInputStream) {
        stream.readNBytes(extraHeaderLength)
    }

    protected open fun readData(stream: DataInputStream) {
        stream.readNBytes(dataLength)
    }

    protected fun checkId(id: Int) {
        if (this.id != id) {
            throw RuntimeException("Invalid Id Chunk")
        }
    }

    private fun buildHeader(): ByteArray
    {
        val out = DataByteArrayOutputStream()
        out.writeByte(getId())
        out.writeShortLE(extraHeaderLength)
        out.writeShortLE(dataLength)
        return out.toByteArray()
    }

    protected fun ensemble(extraHeader: ByteArray, data: ByteArray): ByteArray
    {
        val out = DataByteArrayOutputStream()

        extraHeaderLength = extraHeader.size
        dataLength = data.size

        out.write(buildHeader())
        out.write(extraHeader)
        out.write(data)

        return out.toByteArray()
    }
}