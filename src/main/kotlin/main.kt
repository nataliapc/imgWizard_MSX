import org.nataliapc.imagewizard.swing.ViewFrame
import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.compressor.Compressor.Companion.MAX_SIZE_UNCOMPRESSED
import org.nataliapc.imagewizard.compressor.Raw
import org.nataliapc.imagewizard.compressor.Rle
import org.nataliapc.imagewizard.image.ImgXFactory
import org.nataliapc.imagewizard.image.ImgXImpl
import org.nataliapc.imagewizard.image.ImgXRepository
import org.nataliapc.imagewizard.image.chunks.Chunk
import org.nataliapc.imagewizard.image.chunks.impl.*
import org.nataliapc.imagewizard.resourcefiles.ResElementFile
import org.nataliapc.imagewizard.resourcefiles.ResFileImpl
import org.nataliapc.imagewizard.screens.ScreenBitmap
import org.nataliapc.imagewizard.screens.ScreenBitmapImpl
import org.nataliapc.imagewizard.screens.ScreenMSX
import org.nataliapc.imagewizard.screens.enums.*
import org.nataliapc.imagewizard.screens.imagewrapper.ImageWrapperImpl
import org.nataliapc.imagewizard.screens.interfaces.ScreenPaletted
import java.io.File
import java.io.FileInputStream
import java.lang.RuntimeException
import javax.imageio.ImageIO
import kotlin.math.absoluteValue
import kotlin.system.exitProcess
import java.awt.image.BufferedImage
import java.lang.Integer.min


const val version = "1.4.00"
const val appname = "imgWizard"
const val verbose = true


fun main(args: Array<String>)
{
    try {
        if (args.isEmpty()) {
            showHelp()
        } else {
            val time1 = System.currentTimeMillis()
            when (args[0].lowercase()) {
                "l" -> cmdL_ListContent(args)
                "c", "cl" -> cmdCL_CreateImageIMx(args)
//            "s" -> cmdS_CreateImageFromRectangle(args)
                "gs" -> cmdGS_V9990ImageFromRectangle(args)
                "r" -> cmdR_LocationRedirection(args)
                "d" -> cmdD_RemoveChunkFromIMx(args)
                "j" -> cmdJ_JoinImageFiles(args)
                "5a" -> cmd5A_TransformSC5toSC10(args)
                "ca" -> cmdCA_TransformSC12toSC10(args)
                "v" -> cmdV_ViewImageIMx(args)
                "res" -> cmdRES_CreateResourceFile(args)
                else -> showHelp()
            }
            if (verbose) {
                val time2 = System.currentTimeMillis()
                println("Time elapsed: ${"%.2f sec".format((time2 - time1) / 1000.0)}")
            }
        }
    } catch (re: ImgWizardException) {
        showError(re.message ?: "")
    }
}

open class ImgWizardException(message: String, cmd: String = "") : RuntimeException(
    "ERROR: $message${if (cmd.isBlank()) "" else ":\n\t"+commandLineOptions[cmd]}")
class ArgumentException(cmd: String): ImgWizardException("Something wrong with arguments", cmd)
class ArgumentOutOfRangeException(value: Int, cmd: String): ImgWizardException("Argument out of range [$value]", cmd)

// l <file.IM?>
private fun cmdL_ListContent(args: Array<String>)
{
    val fileIn = getFile(args[1])
    ImgXRepository().from(fileIn)
        .printInfo()
}

// c[l] <fileIn.SC?> <lines> [compressor | transparent_color]
fun cmdCL_CreateImageIMx(args: Array<String>)
{
    val cmdIdx = 0
    val fileIdx = 1
    val linesIdx = 2
    val compressorIdx = 3
    val transpIdx = 3
    val cmd = args[cmdIdx].lowercase()

    if (args.size < linesIdx+1 || args.size > compressorIdx+1) {
        throw ArgumentException(cmd)
    }
    val fileIn = getFile(args[fileIdx], "Opening")
    val image = ScreenBitmap.Factory.from(fileIn)
    val lines = checkNumericArg(args[linesIdx])
    val transparent: Byte? = if (args.size==linesIdx+1) null else args[transpIdx].toByteOrNull()
    var compressor: Compressor = Rle(transparent = transparent ?: 0)
    if (transparent == null && args.size == compressorIdx+1) {
        compressor = Compressor.Types.valueOf(args[compressorIdx].uppercase()).instance
    }
    if (lines < 0 || lines > image.height) {
        throw ImgWizardException("Parameter 'lines' exceeds input screen height", cmd)
    }
    if (args[cmdIdx] == "cl") {
        TODO("CL option not yet implemented")
    }

    val imgx = ImgXFactory().getInstance(false)
    imgx.add(DaadClearWindow())
    imgx.header = image.extension.magicHeader

    val dataChunks = splitDataInChunks(image.getRectangle(0, 0, image.width, lines), compressor, ScreenBitmapChunk.MAX_CHUNK_DATA_SIZE)
    dataChunks.forEach {
        if (it.size == ScreenBitmapChunk.MAX_CHUNK_DATA_SIZE) {
            imgx.add(ScreenBitmapChunk(it, Raw()))
        } else {
            imgx.add(ScreenBitmapChunk(it, compressor))
        }
    }

    if (image is ScreenPaletted) {
        imgx.add(ScreenPaletteChunk(image.getPalette()))
    }

    val fileOut = File("${fileIn.nameWithoutExtension}.${image.extension.imxExt}")
    println("### Creating file ${fileOut.name}")

    ImgXRepository().save(imgx, fileOut)
    imgx.printInfo()
}

// gs <file.PNG> <colors> <compressor> [<sx> <sy> <nx> <ny> [<dx> <dy>]]
fun cmdGS_V9990ImageFromRectangle(args: Array<String>)
{
    val cmdIdx = 0
    val cmd = args[cmdIdx].lowercase()

    if (args.size != 4 && args.size != 8 && args.size != 10) {
        throw ArgumentException(cmd)
    }
    val fileIn = getFile(args[1], "Opening")

    val compressor = Compressor.Types.valueOf(args[3].uppercase()).instance
    val pixelType = PixelType.valueOf(args[2].uppercase())
    val paletteType = if (pixelType == PixelType.BD8) PaletteType.GRB332 else PaletteType.GRB555
    val image = ImageWrapperImpl.from(fileIn, pixelType, paletteType)
    val sx = if (args.size <= 4) 0 else checkNumericArg(args[4])
    val sy = if (args.size <= 4) 0 else checkNumericArg(args[5])
    val nx = if (args.size <= 4) image.width else checkNumericArg(args[6])
    val ny = if (args.size <= 4) image.height else checkNumericArg(args[7])
    val dx = if (args.size <= 8) sx else checkNumericArg(args[8])
    val dy = if (args.size <= 8) sy else checkNumericArg(args[9])

    val imgx = ImgXFactory().getInstance().add(V9990CmdChunk.SendRectangle(dx, dy, nx, ny))
    val infoChunk = imgx.get(0) as InfoChunk
    infoChunk.originalWidth = image.width
    infoChunk.originalHeight = image.height
    infoChunk.pixelType = pixelType
    infoChunk.paletteType = paletteType
    infoChunk.chipset = Chipset.V9990

    val dataChunks = splitDataInChunks(image.getRectangle(sx, sy, nx, ny), compressor, Chunk.CHUNK_DATA_SIZE_THREESHOLD)
    dataChunks.forEach {
        if (it.size == Chunk.MAX_CHUNK_DATA_SIZE) {
            imgx.add(V9990CmdDataChunk(it, Raw()))
        } else {
            imgx.add(V9990CmdDataChunk(it, compressor))
        }
    }

    val fileOut = File(fileIn.nameWithoutExtension + ".imx")
    println("### Creating file ${fileOut.name}")

    ImgXRepository().save(imgx, fileOut)
    imgx.printInfo()
}

// r <fileOut.IM?> <target_loc>
fun cmdR_LocationRedirection(args: Array<String>)
{
    val cmdIdx = 0
    val cmd = args[cmdIdx].lowercase()

    if (args.size != 3) {
        throw ArgumentException(cmd)
    }
    val location = checkNumericArg(args[2])
    if (location < 0 || location > 255) {
        throw ImgWizardException("Location is out of range [0..255]", cmd)
    }

    val fileOut = File(args[1])
    println("### Creating file ${fileOut.name}")

    val imgx = ImgXFactory().getInstance(false).add(DaadRedirectToImage(location))
    ImgXRepository().save(imgx, fileOut)
    imgx.printInfo()
}

// d <fileIn.IM?> <chunk_id>
fun cmdD_RemoveChunkFromIMx(args: Array<String>)
{
    val cmdIdx = 0
    val cmd = args[cmdIdx].lowercase()

    if (args.size != 3) {
        throw ArgumentException(cmd)
    }
    val chunkToRemove = checkNumericArg(args[2])
    val fileIn = getFile(args[1])
    val imgx = ImgXRepository().from(fileIn)

    println("    Removing chunk #$chunkToRemove")
    imgx.remove(chunkToRemove)

    println("### Saving file ${fileIn.name}")
    ImgXRepository().save(imgx, fileIn)
}

// j <fileOut.IM?> <fileIn1.IM?> [fileIn2.IM?] [fileIn3.IM?] ...
fun cmdJ_JoinImageFiles(args: Array<String>)
{
    var cmdIdx = 0
    val cmd = args[cmdIdx++].lowercase()

    if (args.size < 3) {
        throw ArgumentException(cmd)
    }
    val fileOut = File(args[cmdIdx])

    var index = 2
    val imgx = ImgXRepository().from(getFile(args[index++]))
    while (index < args.size) {
        val toJoin = ImgXRepository().from(getFile(args[index++]))
        for (i in 0 until toJoin.chunkCount()) {
            val chunk = toJoin.get(i)
            if (chunk !is InfoChunk) {
                imgx.add(chunk)
            }
        }
    }

    println("### Saving file ${fileOut.name}")
    ImgXRepository().save(imgx, fileOut)
}

// 5a <fileIn.SC5> <fileOut.SCA> [lines]
fun cmd5A_TransformSC5toSC10(args: Array<String>) {
    val cmdIdx = 0
    val sc5Idx = 1
    val scaIdx = 2
    val linesIdx = 3
    val cmd = args[cmdIdx].lowercase()

    if (args.size < 3 || args.size > 4) {
        throw ArgumentException(cmd)
    }
    val lines = if (args.size == 4) checkNumericArg(args[linesIdx]) else 212
    if (lines !in 0..212) {
        throw ArgumentOutOfRangeException(lines, cmd)
    }
    val sc5Image = ScreenBitmap.Factory.from(getFile(args[sc5Idx]))
    val scaImage = ScreenBitmap.Factory.getSC10()
    if (sc5Image !is ScreenBitmapImpl.SC5) {
        throw ImgWizardException("Input file might not be an SC5 file", cmd)
    }

    for (y in 0 until lines) {
        for (x in 0..sc5Image.width) {
            TODO()  //TODO
        }
    }

    val fileOut = File(args[scaIdx])
    scaImage.saveTo(fileOut)
}

// ca <fileIn.SCC> <fileOut.SCA> [lines]
fun cmdCA_TransformSC12toSC10(args: Array<String>) {
    val cmdIdx = 0
    val sc12Idx = 1
    val scaIdx = 2
    val linesIdx = 3
    val cmd = args[cmdIdx].lowercase()

    if (args.size < 3 || args.size > 4) {
        throw ArgumentException(cmd)
    }
    val lines = if (args.size == 4) checkNumericArg(args[linesIdx]) else 212
    if (lines !in 0..212) {
        throw ArgumentOutOfRangeException(lines, cmd)
    }
    val sc12 = ScreenBitmapImpl.from(getFile(args[sc12Idx]))
    val sca = ScreenBitmapImpl.SC10()
    if (sc12 !is ScreenBitmapImpl.SC12) {
        throw ImgWizardException("Input file might not be an SC12 file", cmd)
    }

    for (y in 0 until lines) {
        for (x in 0..sc12.width) {
            TODO()  //TODO
        }
    }

    val fileOut = File(args[scaIdx])
    sca.saveTo(fileOut)
}

// v <file.IM?>
fun cmdV_ViewImageIMx(args: Array<String>) {
    val fileIdx = 1

    val fileIn = getFile(args[fileIdx], "Opening")
    var infoChunk: InfoChunk? = null
    var pixelAspectRatio = PixelAspect.Ratio11

    val origImg: BufferedImage = if (canReadImageExtension(fileIn.extension)) {
        ImageIO.read(FileInputStream(fileIn))
    } else {
        val imgx = ImgXRepository().from(fileIn)
        infoChunk = imgx.getInfoChunk()
        pixelAspectRatio = PixelAspect.getFromInfoChunk(infoChunk)
        imgx.render()
    }

    ViewFrame("$appname $version", fileIn, infoChunk, origImg, pixelAspectRatio)
}

fun cmdRES_CreateResourceFile(args: Array<String>)
{
    var cmdIdx = 0
    val cmd = args[cmdIdx++].lowercase()

    if (args.size < 3) {
        throw ArgumentException(cmd)
    }
    val fileOut = File(args[cmdIdx++])
    val fileInc = File((fileOut.parentFile?.absolutePath ?: ".") + File.separator + fileOut.nameWithoutExtension + "_res.h")

    val compressor = Compressor.Types.valueOf(args[cmdIdx++].uppercase()).instance

    val resFile = ResFileImpl(compressor)
    while (cmdIdx < args.size) {
        val resItem = ResElementFile(File(args[cmdIdx++]))
        println("    Adding '${resItem.getName()}'")
        resFile.addResource(resItem)
    }

    println("### Saving Resources file ${fileOut.name}")
    fileOut.writeBytes(resFile.build(true))
    println("### Saving Include file ${fileInc.name}")
    fileInc.writeText(resFile.generateInclude())
}

private fun getFile(filename: String, verb: String = "Reading"): File
{
    val file = File(filename)
    println("### $verb file ${file.name}")
    if (!file.exists()) {
        throw ImgWizardException("File not exists...")
    }
    if (!file.isFile) {
        throw ImgWizardException("Input file is not a file...")
    }
    return file
}

private fun checkNumericArg(value: String): Int
{
    val result = value.toIntOrNull()
    if (result== null) {
        throw RuntimeException("Argument is not numeric: $value")
    } else {
        return result
    }
}

private fun splitDataInChunks(dataIn: ByteArray, compressor: Compressor, maxChunkDataSize: Int): List<ByteArray>
{
    val maxSize = MAX_SIZE_UNCOMPRESSED
    val out = arrayListOf<ByteArray>()
    var start = 0
    var end: Int
    var lastEnd: Int
    var aux: Int
    var dataCompressed: ByteArray
    var dataCompressedSize: Int

    while (start < dataIn.size) {
        end = start + maxChunkDataSize * 2
        if (end > dataIn.size) {
            end = dataIn.size
        } else {
            // Search a near and easy entry point
            do {
                if (end-start >= maxSize) { end = start + maxSize ; break }
                dataCompressed = compressor.compress(dataIn.copyOfRange(start, end))
                dataCompressedSize = dataCompressed.size
                if (verbose) printCompressionProgress(end, dataIn.size, end-start, dataCompressedSize)
                if (dataCompressedSize > maxChunkDataSize) break;
                end = start + (end - start) * 3 / 2
                if (end > dataIn.size) {
                    end = dataIn.size
                    break;
                }
            } while (true)
        }
        // old entry point: end = dataIn.size
        if (end-start > maxSize) {
            end = start + maxSize
        }
        lastEnd = start
        do {
            dataCompressed = compressor.compress(dataIn.copyOfRange(start, end))
            dataCompressedSize = dataCompressed.size
            if (verbose) printCompressionProgress(end, dataIn.size, end-start, dataCompressedSize)
            if (end-start == maxSize && dataCompressedSize <= maxChunkDataSize) break
            if ((lastEnd-end).absoluteValue <= 1 && dataCompressedSize > maxChunkDataSize) {
                end -= 1 ; lastEnd = end ; continue
            }
            if ((lastEnd-end).absoluteValue <= 1 || dataIn.size-start <= maxChunkDataSize-1) break
            if (dataCompressedSize >= maxChunkDataSize-2 && dataCompressedSize <= maxChunkDataSize) break
            aux = end
            if (dataCompressedSize > maxChunkDataSize) {
                end -= (end-lastEnd).absoluteValue / 2
            } else {
                end += (end-lastEnd).absoluteValue / 2
                if (end >= dataIn.size) {
                    end = dataIn.size
                    break
                }
            }
            lastEnd = aux
        } while (true)
        if (end-start <= dataCompressedSize) {
            end = min(dataIn.size, start + maxChunkDataSize)
            dataCompressedSize = end - start
        }
        if (verbose) printCompressionProgress(end, dataIn.size, end-start, dataCompressedSize, true)
        out.add(dataIn.copyOfRange(start, end))
        start = end
    }
    return out
}

private fun canReadImageExtension(fileExt: String): Boolean {
    val iter: Iterator<*> = ImageIO.getImageReadersBySuffix(fileExt)
    return iter.hasNext()
}

private fun printCompressionProgress(len: Int, totalLen: Int, uncompressedSize: Int, compressedSize: Int, done: Boolean = false) {
    print("\r\tChunk size: $uncompressedSize -> $compressedSize [${len*100/totalLen}%] ")
    if (done) println("Done            ")
}

private val commandLineOptions = hashMapOf<String, String>(
    "l" to "$appname l <fileIn.IM?>",
    "v" to "$appname v <*.IM?|*.PNG>",
    "c" to "$appname c[l] <fileIn.SC?> <lines> [compressor | transparent_color]",
    "cl" to "$appname c[l] <fileIn.SC?> <lines> [compressor | transparent_color]",
    "s" to "$appname s <fileIn.SC?> <x> <y> <w> <h> [transparent_color]",
    "gs" to "$appname gs <file.PNG> <colors> <compressor> [<sx> <sy> <nx> <ny> [<dx> <dy>]]",
    "r" to "$appname r <fileOut.IM?> <target_loc>",
    "d" to "$appname d <fileIn.IM?> <chunk_id>",
    "j" to "$appname j <fileOut.IM?> <fileIn1.IM?> [fileIn2.IM?] [fileIn3] ...",
    "5a" to "$appname 5a <fileIn.SC5> <fileOut.SCA> [lines]",
    "ca" to "$appname ca <fileIn.SCC> <fileOut.SCA> [lines]",
    "res" to "$appname res <fileOut.RES> [compressor] <resFile1> <resFile2> ..."
)

private fun showHelp(exit: Boolean = true)
{
    println("\n"+
            "IMGWIZARD v$version for MSX2DAAD\n"+
            "===================================================================\n"+
            "A tool to create and manage MSX image files in several screen modes\n"+
            "to be used by MSX2DAAD engine.\n"+
            "\n"+
            "L) List image chunks:\n"+
            "    ${commandLineOptions["l"]}\n"+
            "\n"+
            "V) View image:\n"+
            "    ${commandLineOptions["v"]}\n"+
            "\n"+
            "C) Create an image IMx (CL - Create the palette at last chunk):\n"+
            "    ${commandLineOptions["c"]}\n"+
            "\n"+
            "S) Create an image from a rectangle:\n"+
            "    ${commandLineOptions["s"]}\n"+
            "\n"+
            "GS) Create a V9990 image from a rectangle:\n"+
            "    ${commandLineOptions["gs"]}\n"+
            "\n"+
            "R) Create a location redirection:\n"+
            "    ${commandLineOptions["r"]}\n"+
            "\n"+
            "D) Remove a CHUNK from an image:\n"+
            "    ${commandLineOptions["d"]}\n"+
            "\n"+
            "J) Join several IMx files in just one:\n"+
            "    ${commandLineOptions["j"]}\n"+
            "\n"+
            "5A) Transform a SC5 image to a RGB SC10(SCA) one:\n"+
            "    ${commandLineOptions["5a"]}\n"+
            "\n"+
            "CA) Transform a SC12(SCC) image to a YJK SC10(SCA) one:\n"+
            "    ${commandLineOptions["ca"]}\n"+
            "\n"+
            "RES) Create a RESource file and an Include file from resource files:\n"+
            "    ${commandLineOptions["res"]}\n"+
            "\n"+
            " <fileIn>      Input file in format SCx (SC5/SC6/SC7/SC8/SCA/SCC)\n"+
            "               Palette can be inside SCx file or PL5 PL6 PL7 files.\n"+
            " <lines>       Image lines to get from input file.\n"+
            " [compressor]  Compression type: RAW, RLE or PLETTER.\n"+
            "                 RAW: no compression but fastest load.\n"+
            "                 RLE: light compression but fast load (default).\n"+
            "                 PLETTER: high compression but slow.\n"+
            "                 PLETTEREXT: Like PLETTER but w/ external compressor binary.\n"+
            "                 ZX7EXT: ZX7 compressor w/ external compressor binary.\n"+
            "                 ZX0EXT: ZX0 compressor w/ external compressor binary.\n"+
            " [transparent] Optional: the color index that will become transparent (decimal).\n"+
            "               Compression is forced to RLE.\n"+
            " <target_loc>  Target location number to redirect to.\n"+
            "                 ex: a 12 redirects to image 012.IMx\n"+
            " <res>         Resolution for V9990 mode. Valid values:\n"+
            "                 B1 for 256x212\n"+
            "                 B3 for 512x212\n"+
            "                 B5 for 640x400\n"+
            "                 B6 for 640x480\n"+
            "                 B7 for 1024x212\n"+
            " <colors>      Color schema for V9990 mode. Valid values:\n"+
            "                 BP2 for 4 colors (from 32768 colors palette)\n"+
            "                 BP4 for 16 colors (from 32768 colors palette)\n"+
            "                 BD8 for 256 colors (from 32768 colors palette)\n"+
            "                 BD16 for 32768 fixed colors (GRB555)\n"+
            " <resFile>     Resource file to add to a RESource compilation\n"+
            "\n"+
            "Example: $appname c image.sc8 96 rle\n"+
            "\n")
    if (exit) {
        exitProcess(1)
    }
}

fun showError(msg: String)
{
    println(msg)
    exitProcess(1)
}
