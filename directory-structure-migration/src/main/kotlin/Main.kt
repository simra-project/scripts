import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import me.tongfei.progressbar.ProgressBar
import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()

fun main(args: Array<String>) {
    val conf = mainBody { ArgParser(args).parseInto(::Conf) }

    logger.info("Config: $conf")

    val rideFiles = getRideFiles(conf.regionDir)
    val mapping = createFileTargetMapping(rideFiles)
    moveFiles(mapping)

    logger.info("Done.")
}

fun getRideFiles(regionDir: File): List<File> {
    return regionDir.walk().toList()
        .filter { it.absolutePath.contains("Rides/VM") || it.absolutePath.contains("Rides${File.separator}VM") }
}

fun createFileTargetMapping(rideFiles: List<File>): Map<File, String> {
    return rideFiles
        .map { Ride(it) }
        .map { it.originalFile to it.getTargetFilePath() }
        .toMap()
}

fun moveFiles(mapping: Map<File, String>) {
    val pb = ProgressBar("Moving Rides", mapping.size.toLong())

    for ((rideFile, targetPath) in mapping) {
        val targetFile = File(targetPath)
        // mkdirs merges dirs if there is only a single subfolder
        targetFile.parentFile.parentFile.mkdir() // create year
        targetFile.parentFile.mkdir() // create month

        val success = rideFile.renameTo(targetFile)
        if (!success) {
            logger.error("File ${rideFile.name} could not be moved to $targetPath")
        }
        pb.step()
    }

    pb.close()
}