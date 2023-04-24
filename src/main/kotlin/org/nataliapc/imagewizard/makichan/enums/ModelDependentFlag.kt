package org.nataliapc.imagewizard.makichan.enums

import kotlin.reflect.full.isSubclassOf

sealed class ModelDependentFlag(val value: Int)
{
    object MSX_SC7I : ModelDependentFlag(0b00000000)    //$00    16     1:1     MSX2 screen mode 7, 512x212
    object MSX_SC7 :  ModelDependentFlag(0x00000100)    //$04    16     1:2     MSX2 screen mode 7, 512x212
    object MSX_SC8 :  ModelDependentFlag(0x00010100)    //$14    256    1:1     MSX2 screen mode 8, 256x212
    object MSX_SC10 : ModelDependentFlag(0x00100100)    //$24    12499+ 1:1     MSX2+ screen mode 10, 256x212
    object MSX_SC11 : ModelDependentFlag(0x00110100)    //$34    12499+ 1:1     MSX2+ screen mode 11, 256x212
    object MSX_SC12 : ModelDependentFlag(0x01000100)    //$44    19268  1:1     MSX2+ screen mode 12, 256x212
    object MSX_SC5 :  ModelDependentFlag(0x01010100)    //$54    16     1:1     MSX2 screen mode 5, 256x212
    class  Unknown(value: Int) :  ModelDependentFlag(value)

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

}