package main

import com.fasterxml.jackson.databind.ObjectMapper
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

import org.apache.logging.log4j.LogManager
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.LineString
import org.geojson.LngLatAlt
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

private val logger = LogManager.getLogger()

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val cla = mainBody { ArgParser(args).parseInto(::Conf) }
    val ridesInfo: MutableMap<String, MutableList<String>> = mutableMapOf()
    logger.info("creating " + cla.outputDir.absolutePath +  File.separator + cla.region + "_rides.json and " + cla.outputDir.absolutePath +  File.separator + cla.region + "_rides.html")
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString() == it.name) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    var bboxList: ArrayList<Double>? = null
                    if (cla.bbox != "0.0,0.0,0.0,0.0") {
                        bboxList = ArrayList()
                        cla.bbox.split(",").forEach { it ->
                            bboxList.add(it.toDouble())
                        }
                    }

                    val rideInfo: MutableList<String>? = getGPSPoints(path.absolutePath, bboxList)
                    if (rideInfo != null) {
                        ridesInfo[path.name] = rideInfo
                    }
                }
            }
        }
    }
    printGeoJson(ridesInfo, cla.outputDir, cla.region.toString())
    printHtml(cla.outputDir, cla.region.toString())
    val endTime = System.currentTimeMillis()
    logger.info("Done. Number of Rides: " + ridesInfo.size + ". Execution took " + ((endTime - startTime)/1000) + " seconds.")
}

fun printHtml(outputDir: File, region: String) {

    val doc = Jsoup.parse(HtmlTemplate(region).html)

    Paths.get(outputDir.absolutePath +  File.separator + region + "_rides.html").toFile().writeText(doc.toString())

}

/**
 * Pretty prints a geoJson file where the rides are written as lineStrings.
 */
fun printGeoJson(rideInfos: MutableMap<String, MutableList<String>>, outputDir: File, region: String) {
    val featureCollection = FeatureCollection()
    rideInfos.forEach { rideInfo ->
        val lineString = LineString()
        rideInfo.value.forEach { element ->
            lineString.add(LngLatAlt(element.split(",")[1].toDouble(), element.split(",")[0].toDouble()))
        }
        val rideName = rideInfo.key
        featureCollection.add(Feature().apply {
            geometry = lineString
            properties = mapOf("ride" to rideName)
        })
    }
    ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get(outputDir.absolutePath +  File.separator + region + "_rides.json").toFile(), featureCollection)
}

/**
 * Traverses a given ride and returns a List of GPS and timestamps (lat,lon,ts) or null, if given bbox is not null and
 * ride does not go through bbox
 * @param path - The absolute path to the ride file
 * @param bbox - (optional) if given, checks if the ride goes through the bbox.
 * @return gps and timestamp of given ride
 */
fun getGPSPoints(path: String, bbox: ArrayList<Double>?): MutableList<String>? {
    var result: MutableList<String>? = mutableListOf() // contains the result
    val inputStream: InputStream = File(path).inputStream() // read the file
    var ridePart = false // set to true after the ride header "lat,lon,X,Y,Z" is passed
    var goesThroughBBox = false // set to true if ride goes through the bbox if given any
    // iterate through the ride file
    inputStream.bufferedReader().useLines { lines -> lines.forEach {
        // if ride part is reached...
        if (ridePart) {
            // split the array by comma
            val lineArray = it.split(",")
            // and if the line is a gps line, add lat, lon and ts to result
            if(lineArray[0].isNotEmpty()) {
                val lat = lineArray[0]
                val lon = lineArray[1]
                val ts = lineArray[5]
                // println("$lat,$lon,$ts")
                result?.add(("$lat,$lon,$ts"))
                if (bbox != null) {
                    if (inBoundingBox(bbox[0],bbox[1],bbox[2],bbox[3],lat.toDouble(),lon.toDouble())) {
                        goesThroughBBox = true
                    }
                }
            }
        }
        // set ridePart to true, if header "lat,lon,X,Y,Z" is reached
        if(it.startsWith("lat,lon,X,Y,Z")) {
            ridePart = true
        }
    } }
    if (bbox != null && !goesThroughBBox) {
        result = null
    }
    return result
}

fun inBoundingBox(blLat: Double, blLon: Double, trLat: Double, trLon:Double, pLat:Double, pLon:Double): Boolean {
    // in case longitude 180 is inside the box

    return pLat in blLat..trLat && pLon in blLon..trLon
}