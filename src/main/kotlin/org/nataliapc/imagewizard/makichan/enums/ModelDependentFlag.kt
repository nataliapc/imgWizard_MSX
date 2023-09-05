package org.nataliapc.imagewizard.makichan.enums

import org.nataliapc.imagewizard.screens.ScreenMSX
import org.nataliapc.imagewizard.screens.enums.PixelType
import org.nataliapc.imagewizard.screens.enums.ScreenModeType
import kotlin.reflect.full.isSubclassOf

sealed class ModelDependentFlag(val value: Int, val pixelType: PixelType)
{
    object MSX_SC7I : ModelDependentFlag(0b00000000, PixelType.BP4)    //$00    16     1:1     MSX2 screen mode 7, 512x212
    object MSX_SC7 :  ModelDependentFlag(0b00000100, PixelType.BP4)    //$04    16     1:2     MSX2 screen mode 7, 512x212
    object MSX_SC8 :  ModelDependentFlag(0b00010100, PixelType.BD8)    //$14    256    1:1     MSX2 screen mode 8, 256x212
    object MSX_SC10 : ModelDependentFlag(0b00100100, PixelType.BYJKP)  //$24    12499+ 1:1     MSX2+ screen mode 10, 256x212
    object MSX_SC11 : ModelDependentFlag(0b00110100, PixelType.BYJKP)  //$34    12499+ 1:1     MSX2+ screen mode 11, 256x212
    object MSX_SC12 : ModelDependentFlag(0b01000100, PixelType.BYJK)   //$44    19268  1:1     MSX2+ screen mode 12, 256x212
    object MSX_SC5 :  ModelDependentFlag(0b01010100, PixelType.BP4)    //$54    16     1:1     MSX2 screen mode 5, 256x212
    class  Unknown(value: Int) :  ModelDependentFlag(value, PixelType.Unspecified)

    companion object {
        fun byValue(value: Int): ModelDependentFlag
        {
            ModelDependentFlag::class.nestedClasses
                .filter { klass -> klass.isSubclassOf(ModelDependentFlag::class) }
                .map { klass -> klass.objectInstance }
                .filterIsInstance<ModelDependentFlag>()
                .forEach {
                    if (it.value == value) {
                        return it
                    }
                }
            return Unknown(value)
        }
    }

    fun name() = this::class.java.simpleName

}