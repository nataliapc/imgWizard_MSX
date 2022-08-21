package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.utils.LittleEndianByteBuffer


/*
    Chunk V9990 Send Command:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (32)
        0x0001  2    Chunk data length (always 21 bytes)
        0x0003  2    Empty chunk header (0x0000)
        ---data---
        0x0005 ...   RAW VDP Command data (21 bytes length)
 */
sealed class V9990CmdChunk(
    val sx: Int,            // word
    val sy: Int,            // word
    val dx: Int,            // word
    val dy: Int,            // word
    val nx: Int,            // word
    val ny: Int,            // word
    val arg: Short,         // byte
    val log: LogicalOp,     // byte
    val mask: Int,          // word
    val foreColor: Int,     // word
    val backColor: Int,     // word
    val cmd: Command        // byte
) : ChunkAbstractImpl(32)
{
    // Operation Commands
    enum class Command(val value: Short) {
        Stop(0b00000000),               // Command being executed is stopped.
        LMMC(0b00010000),               // Data is transferred from CPU to VRAM rectangle area.
        LMMV(0b00100000),               // VRAM rectangle area is painted out.
        LMCM(0b00110000),               // VRAM rectangle area data is transferred to CPU.
        LMMM(0b01000000),               // Rectangle area data is transferred from VRAM to VRAM.
        CMMC(0b01010000),               // CPU character data is color-developed and transferred to VRAM rectangle area.
        CMMM(0b01110000),               // VRAM character data is color-developed and transferred to VRAM rectangle area.
        BMXL(0b10000000),               // Data on VRAM linear address is transferred to VRAM rectangle area.
        BMLX(0b10010000),               // VRAM rectangle area data is transferred onto VRAM linear address.
        BMLL(0b10100000),               // Data on VRAM linear address is transferred onto VRAM linear address.
        Line(0b10110000),               // Straight line is drawn on X/Y-coordinates.
        Search(0b11000000),             // Border color coordinates on X/Y space are detected.
        Point(0b11010000),              // Color code of specified point on X/Y-coordinates is read out.
        Pset(0b11100000),               // Drawing is executed at drawing point on X/Y-coordinates.
        Advance(0b11110000)             // Drawing point on X/Y-coordinates is shifted.
    }

    // Logical operations: 0 0 0 TP L11 L10 L01 L00
    enum class LogicalOp(val value: Short) {
        None(0),
        IMP(0b00001100),                // DC=SC
        AND(0b00001000),                // DC=SC & DC
        OR(0b00001110),                 // DC=SC | DC
        XOR(0b00000110),                // DC=SC ^ DC
        NOT(0b00000011),                // DC=!SC
        TIMP(0b00011100),               // if SC=0 then DC=DC else DC=SC
        TAND(0b00011000),               // if SC=0 then DC=DC else DC=SC & DC
        TOR(0b00011110),                // if SC=0 then DC=DC else DC=SC | DC
        TXOR(0b00010110),               // if SC=0 then DC=DC else DC=SC ^ DC
        TNOT(0b00010011)                // if SC=0 then DC=DC else DC=!SC
    }

    override fun build(): ByteArray {
        val data = LittleEndianByteBuffer.allocate(21)
            .putShort(sx.toShort())
            .putShort(sy.toShort())
            .putShort(dx.toShort())
            .putShort(dy.toShort())
            .putShort(nx.toShort())
            .putShort(ny.toShort())
            .put(arg.toByte())
            .put(log.value.toByte())
            .putShort(mask.toShort())
            .putShort(foreColor.toShort())
            .putShort(backColor.toShort())
            .put(cmd.value.toByte())
            .array()

        return ensemble(data)
    }

    object Stop : V9990CmdChunk(0,0,0,0,0,0,0, LogicalOp.None,0,0, 0, Command.Stop)

    class SendData() :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, 0, 0, Command.LMMC)

    class Fill(posX: Int, posY: Int, width: Int, height: Int, color: Int) :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, color, 0, Command.LMMV)

    class Copy(srcX: Int, srcY: Int, dstX: Int, dstY: Int, width: Int, height: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(srcX, srcY, dstX, dstY, width, height, 0, log, 0xffff, 0, 0, Command.LMMM)

}