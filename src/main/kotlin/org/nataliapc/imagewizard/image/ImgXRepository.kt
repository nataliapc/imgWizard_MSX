package org.nataliapc.imagewizard.image

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception


class ImgXRepository {
    fun from(file: File): ImgX = ImgXImpl.from(file)
    fun from(inputStream: InputStream): ImgX = ImgXImpl.from(inputStream)
    fun save(imgx: ImgX, file: File): Boolean {
        return try {
            FileOutputStream(file).use {
                it.write(imgx.build(true))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}