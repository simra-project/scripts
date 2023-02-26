package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File

class Conf(parser: ArgParser) {
    val simraRoot by parser
        .storing("-s", "--sources", help = "path to source files") { File(this) }
        .default(File("../data/"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }

    val outputDir by parser
        .storing("-r", "--results", help = "path to dir in which results will be stored") { File(this) }
        .default(File("./results/"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }

    val region by parser
        .storing("--region", help = "which region rides to visualize") { File(this) }
        .default("UNDEFINED")
        .addValidator {
            require(value != "UNDEFINED") { "You must supply a region with --region " }

            require(simraRoot.listFiles()!!.toList().map { it.nameWithoutExtension }.contains(value.toString())) {
                "SimRa root folder ${simraRoot.absolutePath} does not contain region $value"
            }
        }

    val o by parser
        .storing("-o", "--overwrite", help = "whether today's files should be overwritten")
        .default(true)

    val bbox by parser
        .storing("-b","--bbox", help = "bounding box (lat1,lon1,lat2,lon2) to include rides that go through it.")
        .default("0.0,0.0,0.0,0.0")
        .addValidator {
            require(value.split(",").size == 4) { "You must provide four coordinates, seperated with a comma like lat1,lon1,lat2,lon2. Each coordinate must be a decimal. " }

            require(value.split(".").size == 5) { "You must provide four coordinates, seperated with a comma like lat1,lon1,lat2,lon2. Each coordinate must be a decimal. " }

            require(value.split(",")[0].toDouble() >= -90 && value.split(",")[0].toDouble() <= 90 && value.split(",")[2].toDouble() >= -90 && value.split(",")[2].toDouble() <= 90)
            {"Latitude ranges between -90 and 90 degrees, inclusive are accepted"}

            require(value.split(",")[1].toDouble() >= -180 && value.split(",")[3].toDouble() <= 180 && value.split(",")[2].toDouble() >= -180 && value.split(",")[2].toDouble() <= 180)
            {"Longitude ranges between -180 and 180 degrees, inclusive are accepted"}
        }


    override fun toString(): String {
        return "Configuration: source files (${simraRoot.absolutePath}), output directory (${outputDir.absolutePath}), region ($region))"
    }


}