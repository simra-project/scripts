package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File

class CommandLineArguments(parser: ArgParser) {

    val simraRoot by parser
        .storing("-s", "--simraRoot", help = "path to the SimRa dataset root") { File(this) }
        .default(File("./simra_data"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }

    val outputDir by parser
        .storing("-o", "--outputDir", help = "path to directory where to store the output json (overwrites)")
        .default("output_data/")
        .addValidator {
            require(File(value).isDirectory) { "$value is not a directory" }
            require(File(value).exists()) { "$value does not exist" }
        }

    val regions by parser
        .storing("-r", "--regions", help = "regions where to search for rides that suit the bounding box. You can state multiple regions by simply separating them with commas, e.g., \" Berlin,UNKNOWN\". If you want all regions put \"all\" or don't use this option.")
        .default("all")

    val fromDate by parser
        .storing("-f", "--from", help = "timestamp in ms (epoch) as a lower boundary for the ride dates")
        .default("0")

    val toDate by parser
        .storing("-t", "--to", help = "timestamp in ms (epoch) as an upper boundary for the ride dates")
        .default(Long.MAX_VALUE.toString());


    val boundingBox by parser
        .storing("-b", "--boundingBox", help = "Bounding box as {west,south,east,north} as longitude,latitude,longitude,latitude values, e.g, 13.021,52.3772,13.8081,52.6513 for Berlin")
        .default("13.376155,52.513166,13.394614,52.519255")

    /*****************************************************************
     * Generated methods
     ****************************************************************/

    override fun toString(): String {
        return "CommandLineArguments(simraRoot=$simraRoot, outputDir='$outputDir', boundingBox ='$boundingBox', regions ='$regions', fromDate = '$fromDate', toDate = '$toDate')"
    }

}