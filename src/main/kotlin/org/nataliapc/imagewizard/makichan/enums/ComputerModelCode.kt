package org.nataliapc.imagewizard.makichan.enums

import kotlin.reflect.full.isSubclassOf

sealed class ComputerModelCode(val value: Int)
{
    object PC98 : ComputerModelCode(0x00)         //PC-98, X68000, many others
    object X68000 : ComputerModelCode(0x00)
    object MSX : ComputerModelCode(0x03)          //MSX, MSX2, MSX2+; requires special handling
    object X1TB : ComputerModelCode(0x1c)         //X1tb ?..
    object M98SA : ComputerModelCode(0x62)        //98-SA
    object XPST : ComputerModelCode(0x68)         //X68K or XPST, Chironon's Paint System Tool ported to X68000 by Kenna
    object PC98_MPS : ComputerModelCode(0x70)     //MPS images for slightly newer PC-98 models
    object PC88 : ComputerModelCode(0x88)         //PC-88
    object MAC : ComputerModelCode(0x99)          //MAC
    object MPS : ComputerModelCode(0xff)          //MPS images
    class Unknown(customValue: Int) : ComputerModelCode(customValue)

    companion object {
        fun byValue(value: Int): ComputerModelCode
        {
            ComputerModelCode::class.nestedClasses
                .filter { klass -> klass.isSubclassOf(ComputerModelCode::class) }
                .map { klass -> klass.objectInstance }
                .filterIsInstance<ComputerModelCode>()
                .forEach {
                if (it.value == value) {
                    return it
                }
            }
            return Unknown(value)
        }
    }
}