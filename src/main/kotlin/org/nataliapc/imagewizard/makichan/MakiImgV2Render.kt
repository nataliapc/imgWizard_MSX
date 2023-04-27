package org.nataliapc.imagewizard.makichan

import org.nataliapc.imagewizard.makichan.enums.ComputerModelCode
import org.nataliapc.imagewizard.screens.enums.PaletteType
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.utils.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream

class MakiImgV2Render
{
    fun render(imgIn: MakiImgV2): BufferedImage
    {
        val pixelsPerByte = imgIn.header.screenMode.pixelsPerByte()
        val imagePixelWidth = imgIn.paddedImageByteWidth() / pixelsPerByte
        val outputBuffer = IntArray(imgIn.imagePixelWidth() * imgIn.imagePixelHeight() / pixelsPerByte) { 0 }
        val bitCountA = BitCount(ByteArrayInputStream(imgIn.flagA))
        val flagBStream = DataByteArrayInputStream(imgIn.flagB)
        val colorIndex = DataByteArrayInputStream(imgIn.colorIndex)
        //Initialise the action buffer to all zeroes
        val actionBuffer = ActionBuffer(imgIn.paddedImageByteWidth() / 4) { 0 }
        var action: Int
        val backOffset = arrayOf(
            0,
            -1, -2, -4,
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
                for (i in 0..1) {
                    val actionNibble = if (i == 0) { action.shr(4).and(0x0f) } else { action.and(0x0f) }

println("pos: $pos | pixel: ${pos%imagePixelWidth},${pos/imagePixelWidth} <- action(0x${Integer.toHexString(action).padStart(2,'0')}): $actionNibble -> ${backOffset[actionNibble]}")
                    val color = if (actionNibble == 0) {
                        if (colorIndex.available() > 0 ) {
                            val temp = colorIndex.readUnsignedShort()
                            temp
                        } else {
                            0
                        }
                    } else {
// 0x4366
println("$pos ${backOffset[actionNibble]}")
                        val backPos = pos + backOffset[actionNibble]
                        outputBuffer[backPos]
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

        //Prepare palette
        val palette = IntArray(imgIn.header.paletteRaw.size / 3)
        for (index in palette.indices) {
            val g = imgIn.header.paletteRaw[index*3+0].toUByte().toInt()
            val r = imgIn.header.paletteRaw[index*3+1].toUByte().toInt()
            val b = imgIn.header.paletteRaw[index*3+2].toUByte().toInt()

            palette[index] = when(imgIn.header.computerModelCode) {
                is ComputerModelCode.MSX -> PaletteType.GRB333.toRGB24(
                        g.shr(5).shl(8) or
                        r.shr(5).shl(4) or
                        b.shr(5)
                    )
                is ComputerModelCode.XPST -> PaletteType.GRB555.toRGB24(
                        g.shr(3).shl(10) or
                        r.shr(3).shl(5) or
                        b.shr(3)
                    )
                else -> { 0 }
            }
println(Integer.toHexString(palette[index]))
        }

        //Convert Integer output to RGB24 pixels
        val pixelBuffer = ArrayList<Int>()
        val pixelType = imgIn.header.modelDependentFlag.pixelType
        outputBuffer.forEach {
            when (pixelType) {
                PixelType.BP4 -> {
                    pixelBuffer.add(palette[it.shr(12).and(0x0f)])
                    pixelBuffer.add(palette[it.shr(8).and(0x0f)])
                    pixelBuffer.add(palette[it.shr(4).and(0x0f)])
                    pixelBuffer.add(palette[it.shr(0).and(0x0f)])
                }
                PixelType.BD8 -> {
                    pixelBuffer.add(PaletteType.GRB332.toRGB24(it.shr(8).and(0xff)))
                    pixelBuffer.add(PaletteType.GRB332.toRGB24(it.shr(0).and(0xff)))
                }
                else -> {
                    throw RuntimeException("Unexpected palette: ${pixelType.name}")
                }
            }
        }

        val imgOut = BufferedImage(imgIn.imagePixelWidth(), imgIn.imagePixelHeight(), BufferedImage.TYPE_INT_RGB)
println("size: ${imgOut.width} x ${imgOut.height}")
println("outputSize: ${outputBuffer.size}")
//outputBuffer.addAll(IntArray(imgOut.width*imgOut.height - outputBuffer.size) { 0 }.toList())
        imgOut.setRGB(0, 0, imgOut.width, imgOut.height, pixelBuffer.toIntArray(), 0, imgOut.width)
        return imgOut
    }

    //"Flag B" is an array of nibbles (4-bit values), read one byte at a time, and processed top nibble first.
    //These are XORred into the action buffer.
//    private fun getNextFlagB(): Int = flagBStream.nextNibble()
/*
    fun uncompress(flagA, flagB, colorIndex): ArrayList<Int>
    {

    }
*/
    //"Flag A" is a stream of single-bit boolean flags, read one bit at a time, from highest to lowest bit in each byte.
    //These indicate whether to fetch the next "flag B" byte or not.
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