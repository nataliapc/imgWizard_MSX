package org.nataliapc.imagewizard.makichan

import org.nataliapc.imagewizard.makichan.enums.ComputerModelCode
import org.nataliapc.imagewizard.makichan.enums.ModelDependentFlag
import org.nataliapc.imagewizard.makichan.enums.ScreenMode
import org.nataliapc.utils.DataByteArrayInputStream
import org.nataliapc.utils.readUnsignedIntLE
import org.nataliapc.utils.readUnsignedShortLE
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.InputStream
import java.nio.charset.Charset


class HeaderV2 private constructor()
{
    lateinit var magicHeader: String
    lateinit var computerModel: String
    lateinit var metadata: String
    lateinit var computerModelCode: ComputerModelCode
    lateinit var modelDependentFlag: ModelDependentFlag
    lateinit var screenMode: ScreenMode
    var leftX: Int = 0
    var topY: Int = 0
    var rightX: Int = 0
    var bottomY: Int = 0
    var offsetFlagA: Long = 0
    var offsetFlagB: Long = 0
    var sizeFlagB: Long = 0
    var offsetColorIndex: Long = 0
    var sizeColorIndex: Long = 0
    lateinit var paletteRaw: ByteArray

    companion object {
        fun from(inputStream: InputStream): HeaderV2
        {

            val stream = DataInputStream(inputStream)
            val header = HeaderV2()

            //0    8    Signature "MAKI02  " with two spaces at the end
            header.magicHeader = String(stream.readNBytes(8))
            //8    4    Computer model or image editor that the image was saved on, e.g. PC98, MSX+, MPS, XPST
            header.computerModel = String(stream.readNBytes(4))
            //12   ..   User name etc. metadata string, encoded in Shift-JIS; Variable-length, terminates with
            //          byte $1A, and the first 00 after $1A marks the start of the real header
            val tmpString = mutableListOf<Byte>()
            while (true) {
                val value = stream.read()
                if (value == 0x1a) break
                tmpString.add(value.toByte())
            }
            header.metadata = String(tmpString.toByteArray(), Charset.forName("Shift_JIS"))
println("Metadata: ${header.metadata}")

            //0    1    Start of header, always 00
            stream.skip(1)
            //1    1    Computer model code
            header.computerModelCode = ComputerModelCode.byValue(stream.readUnsignedByte())
            //2    1    Model-dependent flags
            header.modelDependentFlag = ModelDependentFlag.byValue(stream.readUnsignedByte())
            //3    1    Screen mode
            header.screenMode = ScreenMode.byValue(stream.read())
            //4    2    X coordinate for image's left edge
            header.leftX = stream.readUnsignedShortLE()
            //6    2    Y coordinate for image's top edge
            header.topY = stream.readUnsignedShortLE()
            //8    2    X coordinate for image's right edge
            header.rightX = stream.readUnsignedShortLE()
            //10   2    Y coordinate for image's bottom edge
            header.bottomY = stream.readUnsignedShortLE()
            //12   4    Offset from start of header to "flag A" stream
            header.offsetFlagA = stream.readUnsignedIntLE()
            //16   4    Offset from start of header to "flag B" stream
            header.offsetFlagB = stream.readUnsignedIntLE()
            //20   4    Size of "flag B" stream, in bytes
            header.sizeFlagB = stream.readUnsignedIntLE()
            //24   4    Offset from start of header to "color index" stream
            header.offsetColorIndex = stream.readUnsignedIntLE()
            //28   4    Size of "color index" stream, in bytes
            header.sizeColorIndex = stream.readUnsignedIntLE()
            //32   ..   Palette: up to 256 byte triplets, order GRB
            val paletteSize = header.modelDependentFlag.pixelType.colors * 3
            header.paletteRaw = stream.readNBytes(paletteSize)

            return header
        }
    }
}