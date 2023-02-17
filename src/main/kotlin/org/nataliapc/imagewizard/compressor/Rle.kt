package org.nataliapc.imagewizard.compressor

import org.nataliapc.utils.DataByteArrayInputStream
import org.nataliapc.utils.DataByteArrayOutputStream
import org.nataliapc.utils.writeShortLE
import java.lang.Integer.min
import java.lang.RuntimeException


class Rle(private val addSize: Boolean = false,
          private val eof: Boolean = true,
          private val mark: Byte? = null,
          private val transparent: Byte? = null) : CompressorImpl(1)
{
    override fun compress(data: ByteArray): ByteArray {
        val out = DataByteArrayOutputStream()

        if (addSize) {
            out.writeShortLE(data.size)
        }

        //Find mark byte
        val markByte: Byte
        if (mark == null) {
            val bytes = IntArray(256) { 0 }
            data.forEach {
                bytes[it.toUByte().toInt()]++
            }
            markByte = bytes.indexOf(bytes.minOrNull()!!).toByte()
        } else {
            markByte = mark
        }
        out.writeByte(markByte.toInt())

        //Compress RLE
        var v: Byte
        var i = 0
        var j: Int
        while (i < data.size) {
            v = data[i]
            j = 0
            while (i + j < data.size && data[i+j] == v) {
                j++
            }
            if (j > 3 || v == markByte || v == transparent) {
                j = min(j, 255)
                if (v == transparent) {             //Transparent compression
                    out.writeByte(markByte.toInt())
                    out.writeByte(2)
                    out.writeByte(j)
                } else {                            //Normal compression
                    if (v == markByte && j <= 3) { j = 1 }
                    out.writeByte(markByte.toInt())
                    out.writeByte(j)
                    out.writeByte(v.toInt())
                }
                i = i + j - 1
            } else {
                out.writeByte(v.toInt())
            }
            i++
        }
        if (eof) {
            out.writeByte(markByte.toInt())
            out.writeByte(0)
        }

        val result = out.toByteArray()
        out.close()

        return result
    }

    override fun uncompress(data: ByteArray): ByteArray {
        val dataIn = DataByteArrayInputStream(data)
        val out = DataByteArrayOutputStream()

        if (addSize) {
            dataIn.readNBytes(2)
        }

        val mark = dataIn.readByte()
        while (dataIn.available() > 0) {
            val value = dataIn.readByte()
            if (value == mark) {
                val auxValue = dataIn.readByte()
                when (auxValue.toInt()) {
                    0 -> break
                    2 -> {  // Transparent
                        val aux2Value = dataIn.readByte()
                        out.write(ByteArray(aux2Value.toInt()) { 0 })
                    }
                    3 -> throw RuntimeException("Error uncompressing Rle data. Unespected value")
                    else -> {
                        val aux2Value = dataIn.readByte()
                        out.write(ByteArray(auxValue.toUByte().toInt()) { aux2Value })
                    }
                }
            } else {
                out.writeByte(value.toInt())
            }
        }

        val result = out.toByteArray()
        out.close()

        return result
    }
}