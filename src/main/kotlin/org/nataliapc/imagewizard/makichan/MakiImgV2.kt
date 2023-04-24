package org.nataliapc.imagewizard.makichan

import org.nataliapc.imagewizard.screens.enums.PixelAspect


interface MakiImgV2
{
    var header: HeaderV2
    var flagA: ByteArray
    var flagB: ByteArray
    var colorIndex: ByteArray
    fun getPixelAspectRatio(): PixelAspect
    fun paddedLeftEdge(): Int
    fun paddedRightEdge(): Int
    fun paddedImageByteWidth(): Int
    fun paddedImagePixelWidth(): Int
    fun imagePixelWidth(): Int
    fun imagePixelHeight(): Int
}