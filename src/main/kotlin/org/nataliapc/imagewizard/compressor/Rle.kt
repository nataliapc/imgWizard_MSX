package org.nataliapc.imagewizard.compressor

import org.nataliapc.imagewizard.utils.DataByteArrayOutputStream
import org.nataliapc.imagewizard.utils.writeShortLE
import java.lang.Integer.min


class RLE(private val addSize: Boolean = false,
          private val eof: Boolean = true,
          private val mark: Byte? = null,
          private val transparent: Byte? = null) : Compressor
{
    override val id: Int = 1

    override fun compress(data: ByteArray): ByteArray {
        val out = DataByteArrayOutputStream()

        if (addSize) {
            out.writeShortLE(data.size)
        }

        //Find mark byte
        val markByte: Int
        if (mark == null) {
            val bytes = ByteArray(256) { 0 }
            data.forEach {
                bytes[it.toInt()]++
            }
            markByte = bytes.indexOf(bytes.minOrNull()!!)
        } else {
            markByte = mark.toInt()
        }
        out.writeByte(markByte)

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
            if (j > 3 || v == markByte.toByte() || v == transparent) {
                j = min(j, 255)
                if (v == transparent) {             //Transparent compression
                    out.writeByte(markByte)
                    out.writeByte(2)
                    out.writeByte(j)
                } else {                            //Normal compression
                    if (v == markByte.toByte() && j <= 3) { j = 1 }
                    out.writeByte(markByte)
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
            out.writeByte(markByte)
            out.writeByte(0)
        }

        val result = out.toByteArray()
        out.close()

        return result
    }
}