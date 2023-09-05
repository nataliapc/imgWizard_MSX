package org.nataliapc.imagewizard.makichan.enums

import kotlin.reflect.full.isSubclassOf

sealed class ScreenMode(val id: Int, val rows: Int, val colors: Int, val ratio: Int, val description: String)
{
    object R400C16R1 : ScreenMode(0, 400, 16, 1, "PC-98")
    object R200C16R2 : ScreenMode(1, 200, 16, 2, "")
    object R400C8R1 : ScreenMode(2, 400, 8, 1, "VM98")
    object R200C8R2 : ScreenMode(3, 200, 8, 2, "late PC-88")
    object R400C16R1BIS : ScreenMode(4, 400, 16, 1, "")
    object R200C16R2BIS : ScreenMode(5, 200, 16, 2, "")
    object R400C8R1BIS : ScreenMode(6, 400, 8, 1, "early PC-98")
    object R200C8R2BIS : ScreenMode(7, 200, 8, 2, "PC-88")
    object R400C256R1 : ScreenMode(128, 400, 256, 1, "late PC-98")
    object R212C256P1 : ScreenMode(129, 212, 256, 1, "hardware palette?")
    object R212C256 : ScreenMode(132, 212, 256, 1, "")
    object R212C256P2 : ScreenMode(133, 212, 256, 1, "hardware palette?")
    class  Unknown(value: Int) : ScreenMode(value, 0, 0, 0, "")

    fun bpp(): Int = if (id.and(128) == 1) { 8 } else { 4 }
    fun is4bpp(): Boolean = bpp() == 4
    fun is8bpp(): Boolean = bpp() == 8
    fun pixelsPerByte() = 8 / bpp()

    companion object {
        fun byValue(id: Int): ScreenMode
        {
            ScreenMode::class.nestedClasses
                .filter { klass -> klass.isSubclassOf(ScreenMode::class) }
                .map { klass -> klass.objectInstance }
                .filterIsInstance<ScreenMode>()
                .forEach {
                    if (it.id == id) {
                        return it
                    }
                }
            return Unknown(id)
        }
    }

}