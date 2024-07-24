package org.nataliapc.imagewizard.resourcefiles

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

class ResFileRepository {
    fun from(file: File): ResFile = from(file.inputStream())
    fun from(inputStream: InputStream): ResFile = ResFileImpl.from(inputStream)
    fun save(resx: ResFile, file: File): Boolean {
        return try {
            FileOutputStream(file).use {
                it.write(resx.build(true))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}