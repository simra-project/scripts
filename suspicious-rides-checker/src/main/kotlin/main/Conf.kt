package de.hasenburg.broker.simulation.main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import java.io.File

class Conf(parser: ArgParser) {
    val sourceFiles by parser
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

    val o by parser
        .storing("-o", "--overwrite", help = "whether today's files should be overwritten")
        .default(true)

    val copyTo by parser
        .storing("-c", "--copy", help = "filepath to which a copy of the dashboard.json will be stored") { File(this) }
        .default(File("../simra-project.github.io/dashboard/resources/dashboard.json"))
        .addValidator {
            check(value.parentFile.isDirectory) { "Dashboard copy must be stored in a directory "}
            check(value.extension == "json") { "Dashboard must be stored in a json file "}
        }


    override fun toString(): String {
        return "Configuration: source files (${sourceFiles.absolutePath}), output directory (${outputDir.absolutePath}), today's file overwriting ($o), dashboard copy (${copyTo.absolutePath})"
    }


}