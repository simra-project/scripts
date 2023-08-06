package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Conf(parser: ArgParser) {

    val input by parser
        .storing("-i", "--inputDir", help = "path to folder containing accGPS and accEvents files") { File(this) }
        .default(File("../files/"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }

    val outputDir by parser
        .storing("-o", "--outputDir", help = "path to directory where to store the output json, csv and .zip (overwrites)") { File(this) }
        .default(File("./output_data/"))
        .addValidator {
            if (!value.exists()) {
                throw InvalidArgumentException("${value.absolutePath} does not exist")
            }
            if (!value.isDirectory) {
                throw InvalidArgumentException("${value.absolutePath} is not a directory")
            }
        }
}