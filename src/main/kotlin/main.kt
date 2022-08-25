import org.nataliapc.imagewizard.compressor.Compressor
import org.nataliapc.imagewizard.image.ImgXImpl
import org.nataliapc.imagewizard.image.chunks.ChunkAbstractImpl.Companion.MAX_CHUNK_DATA_SIZE
import org.nataliapc.imagewizard.image.chunks.impl.DaadRedirectToImage
import org.nataliapc.imagewizard.image.chunks.impl.InfoChunk
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdChunk
import org.nataliapc.imagewizard.image.chunks.impl.V9990CmdDataChunk
import org.nataliapc.imagewizard.screens.ColorType
import org.nataliapc.imagewizard.screens.PaletteType
import org.nataliapc.imagewizard.screens.ScreenBitmapImpl
import org.nataliapc.imagewizard.screens.imagewrapper.ImageWrapperImpl
import java.io.File
import java.io.FileOutputStream
import kotlin.math.absoluteValue
import kotlin.system.exitProcess


const val verbose = true

fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }
fun IntArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%06x".format(eachByte) }

fun main(args: Array<String>)
{
    if (args.isEmpty()) {
        showHelp()
    } else {
        when (args[0].lowercase()) {
            "l" -> cmdL_ListContent(args)
//            "c", "cl" -> cmdCL_CreateImageIMx(args)
//            "s" -> cmdS_CreateImageFromRectangle(args)
            "gs" -> cmdGS_V9990ImageFromRectangle(args)
            "r" -> cmdR_LocationRedirection(args)
            "d" -> cmdD_RemoveChunkFromIMx(args)
            "j" -> cmdJ_JoinImageFiles(args)
//            "5a" -> cmd5A_TransformSC5toSC10(args)
//            "ca" -> cmd5A_TransformSC12toSC10(args)
            else -> showHelp()
        }
    }
}

// j <fileOut.IM?> <fileIn1.IM?> [fileIn2.IM?] [fileIn3.IM?] ...
fun cmdJ_JoinImageFiles(args: Array<String>)
{
    if (args.size < 3) {
        showError("Insuficient argmuments")
    }
    val fileOut = File(args[1])

    var index = 2
    val imgx = ImgXImpl.from(getFile(args[index++]))
    while (index < args.size) {
        val toJoin = ImgXImpl.from(getFile(args[index++]))
        for (i in 0 until toJoin.chunkCount()) {
            val chunk = toJoin.get(i)
            if (chunk !is InfoChunk) {
                imgx.add(chunk)
            }
        }
    }

    println("### Saving file ${fileOut.name}")
    val out = FileOutputStream(fileOut)
    out.use {
        out.write(imgx.build())
    }
}

// d <fileIn.IM?> <chunk_id>
fun cmdD_RemoveChunkFromIMx(args: Array<String>)
{
    if (args.size != 3 && args[2].toIntOrNull() != null) {
        showError("Bad argmuments")
    }
    val fileIn = getFile(args[1])
    val imgx = ImgXImpl.from(fileIn)

    println("    Removing chunk #${args[2]}")
    imgx.remove(args[2].toInt())

    println("### Saving file ${fileIn.name}")
    val out = FileOutputStream(fileIn)
    out.use {
        out.write(imgx.build())
    }
}

// c[l] <fileIn.SC?> <lines> [compressor | transparent_color]
fun cmdCL_CreateImageIMx(args: Array<String>)
{
    TODO()
}

// l <file.IM?>
private fun cmdL_ListContent(args: Array<String>)
{
    val fileIn = getFile(args[1])
    ImgXImpl.from(fileIn)
        .printInfo()
}

// gs <file.PNG> <colors> <compressor> [<sx> <sy> <nx> <ny> [<dx> <dy>]]
fun cmdGS_V9990ImageFromRectangle(args: Array<String>)
{
    if (args.size != 4 && args.size != 8 && args.size != 10) {
        showError("Bad argmuments number: ${args.size}")
    }
    val fileIn = getFile(args[1], "Opening")

    val compressor = Compressor.Types.valueOf(args[3].uppercase()).instance
    val colorType = ColorType.valueOf(args[2].uppercase())
    val image = ImageWrapperImpl.from(fileIn, colorType, PaletteType.GRB555)
    val sx = if (args.size == 4) 0 else args[4].toInt()
    val sy = if (args.size == 4) 0 else args[5].toInt()
    val nx = if (args.size == 4) image.getWidth() else args[6].toInt()
    val ny = if (args.size == 4) image.getHeight() else args[7].toInt()
    val dx = if (args.size == 8) sx else args[8].toInt()
    val dy = if (args.size == 8) sy else args[9].toInt()

    val imgx = ImgXImpl().add(V9990CmdChunk.RectangleToSend(dx, dy, nx, ny))
    val dataChunks = splitDataInChunks(image.getRectangle(sx, sy, nx, ny), compressor)
    dataChunks.forEach {
        imgx.add(V9990CmdDataChunk(it, compressor))
    }

    val fileOut = File(fileIn.nameWithoutExtension + ".imx")
    println("### Creating file ${fileOut.name}")

    val out = FileOutputStream(fileOut)
    out.use {
        out.write(imgx.build())
    }
    imgx.printInfo()
}

// r <fileOut.IM?> <target_loc>
fun cmdR_LocationRedirection(args: Array<String>)
{
    if (args.size != 3) {
        showError("Bad arguments number: ${args.size}")
    }
    val location = args[2].toShortOrNull()
    if (location == null || location < 0 || location > 255) {
        showError("Location is not a number or is out of range [0..255]")
    }

    val fileOut = File(args[1])
    println("### Creating file ${fileOut.name}")

    val out = FileOutputStream(fileOut)
    val imgx = ImgXImpl(false).add(DaadRedirectToImage(location!!))
    out.use {
        out.write(imgx.build())
    }
    imgx.printInfo()
}

private fun getFile(filename: String, verb: String = "Reading"): File
{
    val file = File(filename)
    println("### $verb file ${file.name}")
    if (!file.exists()) {
        showError("ERROR: file not exists...")
    }
    return file
}

private fun splitDataInChunks(dataIn: ByteArray, compressor: Compressor): List<ByteArray>
{
    val out = arrayListOf<ByteArray>()
    var start = 0
    var end: Int
    var lastEnd: Int
    var aux: Int
    var dataCompressed: ByteArray

    while (start < dataIn.size) {
        end = dataIn.size
        lastEnd = start
        do {
            dataCompressed = compressor.compress(dataIn.copyOfRange(start, end))
            if (verbose) print("\r\tChunk size: ${end-start} -> ${dataCompressed.size} [${end*100/dataIn.size}%] ")
            if ((lastEnd-end).absoluteValue <= 1 || dataIn.size-start <= MAX_CHUNK_DATA_SIZE-1) break
            if (dataCompressed.size >= MAX_CHUNK_DATA_SIZE-2 && dataCompressed.size <= MAX_CHUNK_DATA_SIZE) break
            aux = end
            if (dataCompressed.size > MAX_CHUNK_DATA_SIZE) {
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
        if (verbose) println(" Done            ")
        out.add(dataIn.copyOfRange(start, end))
        start = end
    }

    return out
}

private fun showHelp(exit: Boolean = true)
{
    val version = "1.4.00"
    val appname = "imgWizard"
    println("\n"+
            "IMGWIZARD v$version for MSX2DAAD\n"+
            "===================================================================\n"+
            "A tool to create and manage MSX image files in several screen modes\n"+
            "to be used by MSX2DAAD engine.\n\n"+
            "L) List image chunks:\n"+
            "    $appname l <fileIn.IM?>\n"+
            "\n"+
            "C) Create an image IMx (CL - Create the palette at last chunk):\n"+
            "    $appname c[l] <fileIn.SC?> <lines> [compressor | transparent_color]\n"+
            "\n"+
            "S) Create an image from a rectangle:\n"+
            "    $appname s <fileIn.SC?> <x> <y> <w> <h> [transparent_color]\n"+
            "\n"+
            "GS) Create a V9990 image from a rectangle:\n"+
            "    $appname gs <file.PNG> <colors> <compressor> [<sx> <sy> <nx> <ny> [<dx> <dy>]]\n"+
            "\n"+
            "R) Create a location redirection:\n"+
            "    $appname r <fileOut.IM?> <target_loc>\n"+
            "\n"+
            "D) Remove a CHUNK from an image:\n"+
            "    $appname d <fileIn.IM?> <chunk_id>\n"+
            "\n"+
            "J) Join several IMx files in just one:\n"+
            "    $appname j <fileOut.IM?> <fileIn1.IM?> [fileIn2.IM?] [fileIn3] ...\n"+
            "\n"+
            "5A) Transform a SC5 image to a RGB SC10(SCA) one:\n"+
            "    $appname 5a <fileIn.SC5> <fileOut.SCA> <lines>\n"+
            "\n"+
            "CA) Transform a SC12(SCC) image to a YJK SC10(SCA) one:\n"+
            "    $appname ca <fileIn.SCC> <fileOut.SCA> <lines>\n"+
            "\n"+
            " <fileIn>      Input file in format SCx (SC5/SC6/SC7/SC8/SCA/SCC)\n"+
            "               Palette can be inside SCx file or PL5 PL6 PL7 files.\n"+
            " <lines>       Image lines to get from input file.\n"+
            " [compressor]  Compression type: RAW, RLE or PLETTER.\n"+
            "                 RAW: no compression but fastest load.\n"+
            "                 RLE: light compression but fast load (default).\n"+
            "                 PLETTER: high compression but slow.\n"+
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

fun otra()
{
    val sc = ScreenBitmapImpl.SC5()

    val image = ImageWrapperImpl.from(File("img.png"), sc.colorType, sc.paletteType)

    println(image.getOriginalPalette().toHex())
    println(image.getFinalPalette().toHex())

    println(image.getRectangle(7,0,128,1).toHex())

//    ImageIO.read()
/*
JLabel picLabel = new JLabel(new ImageIcon(myPicture));

JPanel jPanel = new JPanel();
jPanel.add(picLabel);

JFrame f = new JFrame();
f.setSize(new Dimension(myPicture.getWidth(), myPicture.getHeight()));
f.add(jPanel);
f.setVisible(true);
*/
}
