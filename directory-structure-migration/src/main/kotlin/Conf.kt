import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import java.io.File

class Conf(parser: ArgParser) {
    val regionDir by parser
        .storing("-r", "--regionDir", help = "path to directory in which the regions are located") { File(this) }
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("No region directory found at ${value.absolutePath}")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("Given region directory ${value.absolutePath} is not a directory")
            }
        }

    override fun toString(): String {
        return "Configuration: region dir (${regionDir.absolutePath})"
    }


}