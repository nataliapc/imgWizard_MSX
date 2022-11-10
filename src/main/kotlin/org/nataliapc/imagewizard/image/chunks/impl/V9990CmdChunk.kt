package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCompanion
import org.nataliapc.imagewizard.utils.*
import java.io.DataInputStream
import java.lang.RuntimeException
import kotlin.math.abs


/*
    Chunk V9990 Send Command:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (32)
        0x0001  2    Extra header length (0)
        0x0003  2    Data length (21)
        --extra header--
        --data--
        0x0005  21   All paremeters packed and ready to send
 */
open class V9990CmdChunk private constructor() : ChunkAbstractImpl(32)
{
    var sx: Int = 0             // word (Reg 32-33)
        protected set
    var sy: Int = 0             // word (Reg 34-35)
        protected set
    var dx: Int = 0             // word (Reg 36-37)
        protected set
    var dy: Int = 0             // word (Reg 38-39)
        protected set
    var nx: Int = 0             // word (Reg 40-41)
        protected set
    var ny: Int = 0             // word (Reg 42-43)
        protected set
    var arg: Short = 0          // byte (Reg 44)
        protected set
    var log = LogicalOp.None    // byte (Reg 45)
        protected set
    var mask: Int = 0           // word (Reg 46-47)
        protected set
    var foreColor: Int = 0      // word (Reg 48-49)
        protected set
    var backColor: Int = 0      // word (Reg 50-51)
        protected set
    var cmd = Command.Stop      // byte (Reg 52)
        protected set

    constructor(
        sx: Int, sy: Int, dx: Int, dy: Int, nx: Int, ny: Int,
        arg: Short, log: LogicalOp, mask: Int,
        foreColor: Int, backColor: Int,
        cmd: Command
    ): this() {
        this.sx = sx ; this.sy = sy; this.dx = dx ; this.dy = dy ; this.nx = nx ; this.ny = ny
        this.arg = arg ; this.log = log ; this.mask = mask
        this.foreColor = foreColor ; this.backColor = backColor
        this.cmd = cmd
    }

    // Operation Commands
    enum class Command(val value: Short, val description: String) {
        Stop(0b00000000, "Stop"),           // Command being executed is stopped.
        LMMC(0b00010000, "LMMC#SendData"),  // Data is transferred from CPU to VRAM rectangle area.
        LMMV(0b00100000, "LMMV#Fill"),      // VRAM rectangle area is painted out.
        LMCM(0b00110000, "LMCM#GetData"),   // VRAM rectangle area data is transferred to CPU.
        LMMM(0b01000000, "LMMM#Copy"),      // Rectangle area data is transferred from VRAM to VRAM.
        CMMC(0b01010000, "CMMC"),           // CPU character data is color-developed and transferred to VRAM rectangle area.
        CMMM(0b01110000, "CMMM"),           // VRAM character data is color-developed and transferred to VRAM rectangle area.
        BMXL(0b10000000, "BMXL#LinToRec"),  // Data on VRAM linear address is transferred to VRAM rectangle area.
        BMLX(0b10010000, "BMLX#RecToLin"),  // VRAM rectangle area data is transferred onto VRAM linear address.
        BMLL(0b10100000, "BMLL#LinToLin"),  // Data on VRAM linear address is transferred onto VRAM linear address.
        Line(0b10110000, "Line"),           // Straight line is drawn on X/Y-coordinates.
        Search(0b11000000, "Search"),       // Border color coordinates on X/Y space are detected.
        Point(0b11010000, "Point"),         // Color code of specified point on X/Y-coordinates is read out.
        Pset(0b11100000, "Pset"),           // Drawing is executed at drawing point on X/Y-coordinates.
        Advance(0b11110000, "Advance");     // Drawing point on X/Y-coordinates is shifted.
        companion object {
            fun valueOf(value: Int): Command = valueOf(value.toShort())
            fun valueOf(value: Short): Command {
                values().forEach {
                    if (it.value == value) {
                        return it
                    }
                }
                throw RuntimeException("Unknown V9990 command with value $value")
            }
        }
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
        TNOT(0b00010011);               // if SC=0 then DC=DC else DC=!SC

        companion object {
            fun valueOf(value: Int): LogicalOp = valueOf(value.toShort())
            fun valueOf(value: Short): LogicalOp {
                values().forEach {
                    if (it.value == value) {
                        return it
                    }
                }
                throw RuntimeException("Unknown V9990 logical operator with value $value")
            }
        }
    }

    companion object : ChunkCompanion {
        override fun from(stream: DataInputStream): Chunk {
            val obj = V9990CmdChunk()
            obj.readChunk(stream)
            return obj
        }
    }

    override fun ensembleExtraHeader(): ByteArray = byteArrayOf()

    override fun readExtraHeader(stream: DataInputStream) {
        sx = stream.readUnsignedShortLE()
        sy = stream.readUnsignedShortLE()
        dx = stream.readUnsignedShortLE()
        dy = stream.readUnsignedShortLE()
        nx = stream.readUnsignedShortLE()
        ny = stream.readUnsignedShortLE()
        arg = stream.readUnsignedByte().toShort()
        log = LogicalOp.valueOf(stream.readUnsignedByte())
        mask = stream.readUnsignedShortLE()
        foreColor = stream.readUnsignedShortLE()
        backColor = stream.readUnsignedShortLE()
        cmd = Command.valueOf(stream.readUnsignedByte())
    }

    override fun ensembleData(): ByteArray {
        val out = DataByteArrayOutputStream()
        out.use {
            it.writeShortLE(sx)
            it.writeShortLE(sy)
            it.writeShortLE(dx)
            it.writeShortLE(dy)
            it.writeShortLE(nx)
            it.writeShortLE(ny)
            it.writeByte(arg)
            it.writeByte(log.value)
            it.writeShortLE(mask)
            it.writeShortLE(foreColor)
            it.writeShortLE(backColor)
            it.writeByte(cmd.value)
        }
        return out.toByteArray()
    }

    override fun printInfo() {
        println("V9990 Command($sx,$sy, $dx,$dy, $nx,$ny, $arg, ${log.name}, 0x${Integer.toHexString(mask)}, $foreColor,$backColor, ${cmd.description})")
    }


    class Stop : V9990CmdChunk(0,0,0,0,0,0,0, LogicalOp.None,0,0, 0, Command.Stop)

    class SendRectangle(posX: Int, posY: Int, width: Int, height: Int) :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, 0, 0, Command.LMMC)

    class Fill(posX: Int, posY: Int, width: Int, height: Int, color: Int) :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, color, 0, Command.LMMV)

    class Copy(srcX: Int, srcY: Int, dstX: Int, dstY: Int, width: Int, height: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(srcX, srcY, dstX, dstY, width, height, 0, log, 0xffff, 0, 0, Command.LMMM)

    class Pset(posX: Int, posY: Int, color: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(0, 0, posX, posY, 0, 0, 0, log, 0xffff, color, 0, Command.Pset)

    class LinearToRectangle(address: Int, dx: Int, dy: Int, width: Int, height: Int, inverseX: Boolean = false, inverseY: Boolean = false, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            address and 0xff, (address shr 8) and 0xffff,
            dx, dy, width, height,
            ((if (inverseX) 4 else 0) or (if (inverseY) 8 else 0)).toShort(),
            log, 0xffff, 0, 0, Command.BMXL)

    class RectangleToLinear(sx: Int, sy: Int, width: Int, height: Int, address: Int, inverseX: Boolean = false, inverseY: Boolean = false, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            sx, sy,
            address and 0xff, (address shr 8) and 0xffff,
            width, height,
            ((if (inverseX) 4 else 0) or (if (inverseY) 8 else 0)).toShort(),
            log, 0xffff, 0, 0, Command.BMLX)

    class LinearToLinear(sAddress: Int, dAddress: Int, length: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            sAddress and 0xff, (sAddress shr 8) and 0xffff,
            dAddress and 0xff, (dAddress shr 8) and 0xffff,
            length and 0xff, (length shr 8) and 0xffff,
            0,
            log, 0xffff, 0, 0, Command.BMLL)

    class Line(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, log: LogicalOp = LogicalOp.IMP ) :
        V9990CmdChunk(0,0, x1,y1, abs(x2 - x1),abs(y2-y1), 0, log, 0xffff, color, 0, Command.Line)
    {
        init {
            var arg = 0
            if (x1 > x2) arg = arg or 4
            if (y1 > y2) arg = arg or 8
            this.arg = arg.toShort()
        }
    }

}