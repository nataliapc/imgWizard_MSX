package org.nataliapc.imagewizard.makichan

import java.io.File
import java.io.InputStream


class MakiImgV2Repository
{
    fun from(file: File): MakiImgV2 = from(file.inputStream())
    fun from(inputStream: InputStream): MakiImgV2 = MakiImgV2Impl.from(inputStream)
}