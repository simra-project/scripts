package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.InvalidArgumentException
import com.xenomachina.argparser.default
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Conf(parser: ArgParser) {
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

    val region by parser
        .storing("-r","--region", help = "which region incidents to visualize") { File(this) }
        .default("all")
        .addValidator {
            require(value == "all" || (simraRoot.listFiles()!!.toList().map { it.nameWithoutExtension.lowercase() }.contains(value.toString().lowercase()))) {
                "SimRa root folder ${simraRoot.absolutePath} does not contain region $value"
            }
        }

    val regionList by parser
        .storing("-l", "--regionList", help = "SimRa region list to parse")
        .default("simRa_regions_coords_ID.config")
        .addValidator {
            require(File(value).isFile) { "$value is not a file" }
            require(File(value).exists()) { "$value does not exist" }
        }

    val osmDataDir by parser
        .storing("--osmDir", help = "path to directory in which the by osmPreparation generated meta files can be found")
        .default("osm_data/")
        .addValidator {
            require(File(value).isDirectory) { "$value is not a directory" }
            require(File(value).exists()) { "$value does not exist" }
        }

    override fun toString(): String {
        return "Configuration: source files (${simraRoot.absolutePath}), output directory (${outputDir.absolutePath}), region ($region))"
    }

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

    val osmMetaFile = File(osmDataDir).listFiles()!!.filter { it.name.lowercase().startsWith("${region.toString().lowercase()}_meta") }.last()
    init {
        require(osmMetaFile.exists()) { "${osmMetaFile.absolutePath} does not exist" }
    }

    /** Generate meta files */

    fun toMetaFile(): Unit {

        /** Get current date */

        val todaysDate = LocalDate.now()

        val today = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(todaysDate)

        val exactTimeStamp = LocalDateTime.now()

        /** Grab centroid from meta file */

        val jsonO = JSONObject(osmMetaFile.readLines().joinToString(""))
        val centroid = jsonO["centroid"]
        val longRegionName = readLongRegionName()

        /*************************************************************************************
         * meta file
         *************************************************************************************/

        val metaFile = JSONObject()
        metaFile.put("regionTitle", "SimRa Ereigniskarte für $longRegionName")
        metaFile.put("regionDate", "Karte generiert am $today")
        metaFile.put("timeStamp", exactTimeStamp)

        metaFile.put("mapView",centroid)
        metaFile.put("mapZoom", 12)

        val metaFileStandard = "$outputDir/$region-incidents-meta.json"
        val standardMetaFile = File(metaFileStandard)

        standardMetaFile.writeText(metaFile.toString(2))

    }

    /**
     * reads the long region name from the list of region names (e.g., Berlin -> Berlin/Potsdam)
     */
    private fun readLongRegionName(): String {
        val inputStream: InputStream = File(regionList).inputStream() // read the file
        inputStream.bufferedReader().useLines { lines -> lines.forEach { line ->
            if (!line.startsWith("#") && !line.startsWith("Please Choose") && !line.startsWith("!")) {
                val regionShort = line.split("=")[2].lowercase()
                if (region is File) {
                    if (regionShort == (region as File).name.lowercase()) {
                        return line.split("=")[1]
                    }
                }

            }
        } }
        return region.toString()
    }
}