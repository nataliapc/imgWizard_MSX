package org.nataliapc.imagewizard.screens.enums

enum class ScreenModeType(
    val mode: Int,
    val width: Int,
    val height: Int,
    val interlaced: Interlaced = Interlaced.None,
    val signal: Signal = Signal.Single,
    val overscan: Boolean = false
) {
    B0_NTSC(0,192,240, signal = Signal.NTSC, overscan = true),              // V9990 B0 NTSC
    B0_PAL(0,192,290, signal = Signal.PAL, overscan = true),                // V9990 B0 PAL
    B0_NTSC_I(0,192,240, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B0 NTSC Interlaced
    B0_PAL_I(0,192,290, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B0 PAL Interlaced
    B1(1,256,212),                                                          // SC5i, SC8i, SC10i, SC12i, V9990 B1
    B1_I(1,256,424, Interlaced.Enabled),                                    // SC5i, SC8i, SC10i, SC12i, V9990 B1 Interlaced
    B2_NTSC(2,384,240, signal = Signal.NTSC, overscan = true),              // V9990 B2 NTSC
    B2_PAL(2,384,290, signal = Signal.PAL, overscan = true),                // V9990 B2 PAL
    B2_NTSC_I(2,384,480, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B2 NTSC Interlaced
    B2_PAL_I(2,384,580, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B2 PAL Interlaced
    B3(3,512,212),                                                          // SC6, SC7, V9990 B3
    B3_I(3,512,424, Interlaced.Enabled),                                    // SC6i, SC7i, V9990 B3 Interlaced
    B4_NTSC(4,768,240, signal = Signal.NTSC, overscan = true),              // V9990 B4 NTSC
    B4_PAL(4,768,290, signal = Signal.PAL, overscan = true),                // V9990 B4 PAL
    B4_NTSC_I(4,768,480, Interlaced.Enabled, Signal.NTSC, overscan = true), // V9990 B4 NTSC Interlaced
    B4_PAL_I(4,768,580, Interlaced.Enabled, Signal.PAL, overscan = true),   // V9990 B4 PAL Interlaced
    B5(5,640,400),                                                          // V9990 B5
    B6(6,640,480),                                                          // V9990 B6
    B7(7,1024,212),                                                         // V9990 B7
    B7_I(7,1024,424, Interlaced.Enabled)                                    // V9990 B7 Interlaced
}