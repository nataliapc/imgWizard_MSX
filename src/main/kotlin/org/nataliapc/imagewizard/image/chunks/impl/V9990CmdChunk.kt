package org.nataliapc.imagewizard.image.chunks.impl

import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl
import org.nataliapc.imagewizard.image.chunks.ChunkCreateFrom
import org.nataliapc.utils.*
import java.io.DataInputStream
import java.lang.RuntimeException
import kotlin.math.abs


/*
    Chunk V9990 Send Command:
        Offset Size  Description
        --header--
        0x0000  1    Chunk type  (32)
        0x0001  2    Extra header length (1)
        0x0003  2    Data length (21)
        --extra header--
        0x0005  1    Number of commands to read
        --data--
        0x0006  21   All paremeters packed and ready to send
 */
open class V9990CmdChunk() : ChunkAbstractImpl(32)
{
    private val commands = ArrayList<CommandData>()
    var numCommands: Int = 0
        private set

    constructor(
        sx: Int, sy: Int, dx: Int, dy: Int, nx: Int, ny: Int,
        arg: Short, log: LogicalOp, mask: Int,
        foreColor: Int, backColor: Int,
        cmd: CommandType
    ): this() {
        addCommand(CommandData(sx, sy, dx, dy, nx, ny, arg, log, mask, foreColor, backColor, cmd))
    }

    class CommandData(
        var sx: Int = 0,            // word (Reg 32-33)
        var sy: Int = 0,            // word (Reg 34-35)
        var dx: Int = 0,            // word (Reg 36-37)
        var dy: Int = 0,            // word (Reg 38-39)
        var nx: Int = 0,            // word (Reg 40-41)
        var ny: Int = 0,            // word (Reg 42-43)
        var arg: Short = 0,         // byte (Reg 44)
        var log: LogicalOp = LogicalOp.None,   // byte (Reg 45)
        var mask: Int = 0,          // word (Reg 46-47)
        var foreColor: Int = 0,     // word (Reg 48-49)
        var backColor: Int = 0,     // word (Reg 50-51)
        var cmd: CommandType = CommandType.Stop      // byte (Reg 52)
    ) {
        fun toByteArray(): ByteArray {
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
        fun fromStream(stream: DataInputStream): CommandData {
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
            cmd = CommandType.valueOf(stream.readUnsignedByte())
            return this
        }
    }

    // Operation Commands
    enum class CommandType(val value: Short, val description: String) {
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
            fun valueOf(value: Int): CommandType = valueOf(value.toShort())
            fun valueOf(value: Short): CommandType {
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

    companion object : ChunkCreateFrom {
        const val MAX_CMD_IN_CHUNK = 194

        override fun from(stream: DataInputStream): Chunk {
            val obj = V9990CmdChunk()
            obj.readChunk(stream)
            return obj
        }
    }

    fun addCommand(cmd: CommandData): Boolean {
        if (numCommands >= MAX_CMD_IN_CHUNK) return false
        commands.add(cmd)
        numCommands = commands.size
        return true
    }

    fun getCommand(index: Int): CommandData = commands[index]

    fun removeAt(index: Int) {
        commands.removeAt(index)
        numCommands = commands.size
    }

    override fun ensembleExtraHeader(): ByteArray {
        val out = DataByteArrayOutputStream()
        out.writeByte(numCommands)
        return out.toByteArray()
    }

    override fun readExtraHeader(stream: DataInputStream) {
        numCommands = stream.readUnsignedByte()
    }

    override fun ensembleData(): ByteArray {
        val out = DataByteArrayOutputStream()
        commands.forEach { cmd ->
            out.write(cmd.toByteArray())
        }
        return out.toByteArray()
    }

    override fun readData(stream: DataInputStream) {
        val count = numCommands
        for (i in 1..count) {
            addCommand(CommandData().fromStream(stream))
        }
    }

    override fun printInfo() {
        println("V9990 Commands")
        commands.forEach {
            println("\t(${it.sx},${it.sy}, ${it.dx},${it.dy}, ${it.nx},${it.ny}, ${it.arg}, ${it.log.name}, " +
                "0x${Integer.toHexString(it.mask)}, ${it.foreColor},${it.backColor}, ${it.cmd.description})")
        }
    }


    class Stop : V9990CmdChunk(0,0,0,0,0,0,0, LogicalOp.None,0,0, 0, CommandType.Stop)

    class SendRectangle(posX: Int, posY: Int, width: Int, height: Int) :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, 0, 0, CommandType.LMMC)

    class Fill(posX: Int, posY: Int, width: Int, height: Int, color: Int) :
        V9990CmdChunk(0, 0, posX, posY, width, height, 0, LogicalOp.IMP, 0xffff, color, 0, CommandType.LMMV)

    class Copy(srcX: Int, srcY: Int, dstX: Int, dstY: Int, width: Int, height: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(srcX, srcY, dstX, dstY, width, height, 0, log, 0xffff, 0, 0, CommandType.LMMM)

    class Pset(posX: Int, posY: Int, color: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(0, 0, posX, posY, 0, 0, 0, log, 0xffff, color, 0, CommandType.Pset)

    class LinearToRectangle(address: Int, dx: Int, dy: Int, width: Int, height: Int, inverseX: Boolean = false, inverseY: Boolean = false, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            address and 0xff, (address shr 8) and 0xffff,
            dx, dy, width, height,
            ((if (inverseX) 4 else 0) or (if (inverseY) 8 else 0)).toShort(),
            log, 0xffff, 0, 0, CommandType.BMXL)

    class RectangleToLinear(sx: Int, sy: Int, width: Int, height: Int, address: Int, inverseX: Boolean = false, inverseY: Boolean = false, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            sx, sy,
            address and 0xff, (address shr 8) and 0xffff,
            width, height,
            ((if (inverseX) 4 else 0) or (if (inverseY) 8 else 0)).toShort(),
            log, 0xffff, 0, 0, CommandType.BMLX)

    class LinearToLinear(sAddress: Int, dAddress: Int, length: Int, log: LogicalOp = LogicalOp.IMP) :
        V9990CmdChunk(
            sAddress and 0xff, (sAddress shr 8) and 0xffff,
            dAddress and 0xff, (dAddress shr 8) and 0xffff,
            length and 0xff, (length shr 8) and 0xffff,
            0,
            log, 0xffff, 0, 0, CommandType.BMLL)

    class Line(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, log: LogicalOp = LogicalOp.IMP ) :
        V9990CmdChunk(0,0, x1,y1, abs(x2 - x1),abs(y2-y1), getCorrectArgValue(x1, x2, y1, y2), log, 0xffff, color, 0, CommandType.Line)
    {
        companion object {
            private fun getCorrectArgValue(x1: Int, x2: Int, y1: Int, y2: Int): Short {
                var arg = 0
                if (x1 > x2) arg = arg or 4
                if (y1 > y2) arg = arg or 8
                return arg.toShort()
            }
        }
    }

}