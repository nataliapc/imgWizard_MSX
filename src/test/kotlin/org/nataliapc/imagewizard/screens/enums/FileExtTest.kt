package org.nataliapc.imagewizard.screens.enums

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class FileExtTest {

    @Test
    fun getFileExtension() {
        assertEquals("PNG", FileExt.PNG.fileExtension)
        assertEquals("GIF", FileExt.GIF.fileExtension)
        assertEquals("JPG", FileExt.JPG.fileExtension)
        assertEquals("SC5", FileExt.SC5.fileExtension)
        assertEquals("SC6", FileExt.SC6.fileExtension)
        assertEquals("SC7", FileExt.SC7.fileExtension)
        assertEquals("SC8", FileExt.SC8.fileExtension)
        assertEquals("SCA", FileExt.SCA.fileExtension)
        assertEquals("SCC", FileExt.SCC.fileExtension)
    }

    @Test
    fun getImxExt() {
        assertEquals("IM5", FileExt.SC5.imxExt)
        assertEquals("IM6", FileExt.SC6.imxExt)
        assertEquals("IM7", FileExt.SC7.imxExt)
        assertEquals("IM8", FileExt.SC8.imxExt)
        assertEquals("IMA", FileExt.SCA.imxExt)
        assertEquals("IMC", FileExt.SCC.imxExt)
    }

    @Test
    fun getMagicHeader() {
        assertEquals("IMG5", FileExt.SC5.magicHeader)
        assertEquals("IMG6", FileExt.SC6.magicHeader)
        assertEquals("IMG7", FileExt.SC7.magicHeader)
        assertEquals("IMG8", FileExt.SC8.magicHeader)
        assertEquals("IMGA", FileExt.SCA.magicHeader)
        assertEquals("IMGC", FileExt.SCC.magicHeader)
    }

    @Test
    fun byName() {
        assertEquals(FileExt.SC8, FileExt.byName("SC8"))
    }

    @Test
    fun byName_Error() {
        assertEquals(FileExt.Unknown, FileExt.byName("XXX"))
    }

    @Test
    fun byFileExtension() {
        assertEquals(FileExt.SC8, FileExt.byFileExtension("SC8"))
    }

    @Test
    fun byFileExtension_Error() {
        assertEquals(FileExt.Unknown, FileExt.byFileExtension("XXX"))
    }

    @Test
    fun byMagicHeader() {
        assertEquals(FileExt.SC8, FileExt.byMagicHeader("IMG8"))
    }

    @Test
    fun byMagicHeader_Error() {
        assertEquals(FileExt.Unknown, FileExt.byMagicHeader("XXXX"))
    }
}