import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File

class CommandLineArguments(parser: ArgParser) {

    val simraRoot by parser
        .storing("-s", "--simraRoot", help = "path to source files") { File(this) }
        .default(File("../data/"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }

    val region by parser
        .storing("-r","--region", help = "which region incidents to visualize") { File(this) }
        .default("all")
        .addValidator {
            require(value == "all" || (simraRoot.listFiles()!!.toList().map { it.nameWithoutExtension.lowercase() }.contains(value.toString().lowercase()))) {
                "SimRa root folder ${simraRoot.absolutePath} does not contain region $value"
            }
        }

    val outputDir by parser
        .storing("-o", "--outputDir", help = "path to directory where to store the output (overwrites)")
        .default("output_data/")
        .addValidator {
            require(File(value).isDirectory) { "$value is not a directory" }
            require(File(value).exists()) { "$value does not exist" }
        }


    /*****************************************************************
     * Generated methods
     ****************************************************************/

    override fun toString(): String {
        return "CommandLineArguments(simraRoot='$simraRoot', region='$region', outputDir='$outputDir')"
    }

}