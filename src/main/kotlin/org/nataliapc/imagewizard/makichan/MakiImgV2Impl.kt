package org.nataliapc.imagewizard.makichan

import com.google.gson.Gson
import org.nataliapc.imagewizard.screens.enums.PixelAspect
import org.nataliapc.utils.toHex
import java.awt.image.BufferedImage
import java.io.InputStream

class MakiImgV2Impl private constructor(): MakiImgV2
{
    override lateinit var header: HeaderV2
    override lateinit var flagA: ByteArray
    override lateinit var flagB: ByteArray
    override lateinit var colorIndex: ByteArray

    companion object {
        fun from(stream: InputStream): MakiImgV2 {
            val makiImg = MakiImgV2Impl()
            // Header
            makiImg.header = HeaderV2.from(stream)
            // FlagA
            // TODO: You can ignore the stream sizes listed in the header.
            // Flag A runs up to the start of the flag B stream;
            makiImg.flagA = stream.readNBytes((makiImg.header.offsetFlagB - makiImg.header.offsetFlagA).toInt())
            // FlagB
            // flag B runs up to the start of the color stream;
            makiImg.flagB = stream.readNBytes(makiImg.header.sizeFlagB.toInt() + 1)
            // Color Index
            // the color stream runs to the end of the file.
            makiImg.colorIndex = stream.readNBytes(makiImg.header.sizeColorIndex.toInt())

println("computerModel: ${makiImg.header.computerModel} [${makiImg.header.computerModelCode.value}]")
println("modelDepFlag: ${makiImg.header.modelDependentFlag.name()} [${makiImg.header.modelDependentFlag.value}]")
println("flagA size: ${makiImg.flagA.size}")
println("flagB size: ${makiImg.flagB.size}")
println("color size: ${makiImg.colorIndex.size}")
println("palette size: ${makiImg.header.paletteRaw.size}")
println("pixel/byte width: ${makiImg.imagePixelWidth()} ${makiImg.paddedImageByteWidth()}")
println(makiImg.header.paletteRaw.toHex())

            return makiImg
        }
    }

    override fun getPixelAspectRatio(): PixelAspect {
        return when(header.screenMode.ratio) {
            1 -> PixelAspect.Ratio11
            2 -> PixelAspect.Ratio12
            else -> {
                throw RuntimeException("Not valid aspect ratio (${header.screenMode.ratio})")
            }
        }
    }

    override fun paddedLeftEdge(): Int = (header.leftX / header.screenMode.pixelsPerByte()).and(0xFFFC)
    override fun paddedRightEdge(): Int = (header.rightX / header.screenMode.pixelsPerByte() + 4).and(0xFFFC)
    override fun paddedImageByteWidth(): Int = paddedRightEdge() - paddedLeftEdge()
    override fun paddedImagePixelWidth(): Int = paddedImageByteWidth() * header.screenMode.pixelsPerByte()
    override fun imagePixelWidth(): Int = header.rightX - header.leftX + 1
    override fun imagePixelHeight(): Int = header.bottomY - header.topY + 1

}

