package org.nataliapc.imagewizard.makichan

import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.utils.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream

class MakiImgV2Render
{
    fun render(imgIn: MakiImgV2): BufferedImage
    {
        val imagePixelWidth = imgIn.imagePixelWidth()
        val outputBuffer = IntArray(imgIn.imagePixelWidth() * imgIn.imagePixelHeight() / 4) { 0 }
        val bitCountA = BitCount(ByteArrayInputStream(imgIn.flagA))
        val flagBStream = DataByteArrayInputStream(imgIn.flagB)
        val colorIndex = DataByteArrayInputStream(imgIn.colorIndex)
        val actionBuffer = ActionBuffer(imgIn.paddedImageByteWidth() / 4) { 0 }
        var action = 0    //Initialise the action buffer to all zeroes
        val backOffset = arrayOf(
            0, -1, -2, -4,
            -imagePixelWidth, -imagePixelWidth-1,
            -imagePixelWidth*2, -imagePixelWidth*2-1, -imagePixelWidth*2-2,
            -imagePixelWidth*4, -imagePixelWidth*4-1, -imagePixelWidth*4-2,
            -imagePixelWidth*8, -imagePixelWidth*8-1, -imagePixelWidth*8-2,
            -imagePixelWidth*16
        )

        var pos = 0
        try {
            do {
                //Read the next "flag A" bit.
                val flagA = bitCountA.nextBit()

                //If the "flag A" bit was set, then read the next "flag B" byte and XOR the next value in the action buffer with it; else do nothing.
                if (flagA) {
                    actionBuffer.xor(flagBStream.readUnsignedByte())
                }
                //Read the next action buffer byte (the one you possibly just XORred).
                action = actionBuffer.next()

                //For the top nibble of the action byte:
                //  If the nibble = 0, read the next 16-bit value from the "color index" stream, and output that.
                //  Else copy a 16-bit value from earlier in the output buffer, as described below.
                //Do the same for the bottom nibble of the action byte.
                for (i in 1..2) {
                    val actionNibble = if (i == 1) { action.shr(4).and(0x0f) } else { action.and(0x0f) }

println("pos: $pos | pixel: ${pos%imagePixelWidth},${pos/imagePixelWidth} <- action(0x${Integer.toHexString(action).padStart(2,'0')}): $actionNibble -> ${backOffset[actionNibble]}")
                    val color = if (actionNibble == 0) {
                        try {
                            val colorRead = if (colorIndex.available() > 0 ) {
                                val temp = colorIndex.readUnsignedShortLE()         //TODO es LE o BE ????
println("color: ${Integer.toHexString(temp)}")
                                temp
                            } else {
                                0
                            }
                            PaletteType.GRB555.toRGB24(colorRead)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            0xff00ff
                        }
                    } else {
println("$pos ${backOffset[actionNibble]}")
                        val backPos = pos + backOffset[actionNibble]
try {
                        outputBuffer[backPos]
} catch (e:Exception) { 0 }
                    }
                    outputBuffer[pos++] = color
                }

                //Repeat these steps until the output buffer is full, or one of the input flag streams runs out.
                //  If the "color index" stream runs out, try using zeroes for any extra color indexes until the output buffer is full.
println("bitCountA available: ${bitCountA.available()}")
            } while (pos != outputBuffer.size && flagBStream.available() != 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val palette = IntArray(imgIn.header.paletteRaw.size / 3)
        for (index in palette.indices) {
            palette[index] =
                imgIn.header.paletteRaw[index*3+0].toUByte().toInt().shl(8) or  //G
                imgIn.header.paletteRaw[index*3+1].toUByte().toInt().shl(16) or //R
                imgIn.header.paletteRaw[index*3+2].toUByte().toInt()          //B
println(Integer.toHexString(palette[index]))
        }

        val pixelBuffer = IntArray(imgIn.imagePixelWidth() * imgIn.imagePixelHeight())
        pos = 0
        outputBuffer.forEach {
            pixelBuffer[pos++] = palette[it.shr(12).and(0x0f)]
            pixelBuffer[pos++] = palette[it.shr(8).and(0x0f)]
            pixelBuffer[pos++] = palette[it.shr(4).and(0x0f)]
            pixelBuffer[pos++] = palette[it.shr(0).and(0x0f)]
        }

        val imgOut = BufferedImage(imgIn.imagePixelWidth(), imgIn.imagePixelHeight(), BufferedImage.TYPE_INT_RGB)
println("size: ${imgOut.width} x ${imgOut.height}")
println("outputSize: ${outputBuffer.size}")
//outputBuffer.addAll(IntArray(imgOut.width*imgOut.height - outputBuffer.size) { 0 }.toList())
        imgOut.setRGB(0, 0, imgOut.width, imgOut.height, pixelBuffer, 0, imgOut.width)
        return imgOut
    }

    //"Flag A" is a stream of single-bit boolean flags, read one bit at a time, from highest to lowest bit in each byte.
    //These indicate whether to fetch the next "flag B" byte or not.
//    private fun getNextFlagA(): Boolean = bitCountA.nextBit()

    //"Flag B" is an array of nibbles (4-bit values), read one byte at a time, and processed top nibble first.
    //These are XORred into the action buffer.
//    private fun getNextFlagB(): Int = flagBStream.nextNibble()
/*
    fun uncompress(flagA, flagB, colorIndex): ArrayList<Int>
    {

    }
*/
    internal class BitCount(val stream: InputStream) {
        private var value: Int = 0
        private var pos = 0

        fun nextBit(): Boolean {
            --pos
            if (pos < 0) {
                value = stream.read()
                pos = 7
            }
            return value.and(1.shl(pos)) != 0
        }

        fun available(): Int = stream.available()
    }

    internal class ActionBuffer(initialCapacity: Int) {
        private val buffer = IntArray(initialCapacity)
        private var pos = 0

        constructor(initialCapacity: Int, function: () -> Int) : this(initialCapacity) {
            buffer.fill(function())
        }

        fun xor(value: Int) {
print("flagA ON -> ${Integer.toBinaryString(buffer[pos])} -> ")
            buffer[pos] = buffer[pos].xor(value).and(0xff)
println(Integer.toBinaryString(buffer[pos]))
        }

        fun next(): Int {
            val result = buffer[pos++].and(0xff)
            //While working, loop back to the buffer's beginning when you reach its end.
            if (pos == buffer.size) pos = 0
            return result
        }
    }
/*
    internal class Nibble(val value: Int) {
        fun highNibble(): Int =             return if (highNibble) {
                highNibble = false
                value.shr(4)
            } else {
                val output = value
                value = stream.read()
                highNibble = true
                output
            }.shr(0x0f)
        }

        fun nextValue(): Int {
            val output = value
            value = stream.read()
            return output
        }

        fun getValue(): Int = value
        fun available(): Int = stream.available()
    }
*/
}