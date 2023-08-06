package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder
import java.nio.file.Paths

/**
 * Merges the accEventsX.csv and X_accGps.csv files like they were uploaded to the backend.
 */
private val logger = LogManager.getLogger()
fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val cla = mainBody { ArgParser(args).parseInto(::Conf) }
    var count = 0
    File(cla.input.toURI()).walk().maxDepth(1).forEach { it ->
        if (it.name.contains("accEvents")) {
            val incidentPart = getContentOfAccEvents(it)
            val fileNumber = it.name.replace(".csv","").split("accEvents")[1]
            val accGPSFile = File(it.parent + File.separator + fileNumber + "_accGps.csv")
            val mergedContent = addContentOfAccGps(incidentPart, accGPSFile)
            val pathToCSV = cla.outputDir.absolutePath + File.separator + "VM2_" + fileNumber + ".csv"
            Paths.get(pathToCSV).toFile().writeText(mergedContent)
            count++
        }
    }

    val endTime = System.currentTimeMillis()
    logger.info("Done. Merged $count ride files in ${((endTime - startTime)/1000)} seconds.")

}

fun getContentOfAccEvents(accEventsFile: File): StringBuilder {
    val contentSB = StringBuilder() // contains the incident part
    val inputStream: InputStream = accEventsFile.inputStream() // read the file
    // iterate through the incidents file and copy each line into contentSB
    inputStream.bufferedReader().useLines { lines -> lines.forEach { line ->
        contentSB.appendLine(line)
    } }
    return contentSB
    }

fun addContentOfAccGps(contentSB: StringBuilder, accGPSFile: File): String {
    contentSB.appendLine("")
    contentSB.appendLine("=========================")
    val inputStream: InputStream = accGPSFile.inputStream() // read the file
    // iterate through the incidents file and copy each line into contentSB
    inputStream.bufferedReader().useLines { lines -> lines.forEach { line ->
        contentSB.appendLine(line)
    } }

    return contentSB.toString()
}
