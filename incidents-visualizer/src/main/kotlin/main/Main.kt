package main

import com.fasterxml.jackson.databind.ObjectMapper
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.apache.logging.log4j.LogManager
import org.geojson.*
import java.io.*
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/**
 * Generates a GeoJson, csv and a zip containing both of all incidents in specified region (or all, if no region is given)
 * Also creates a meta file for displaying a message with date and region name.
 */
private val logger = LogManager.getLogger()
private val incidentHeader = arrayOf("lat","lon","ts","bike","childCheckBox","trailerCheckBox","pLoc","incident","i1","i2","i3","i4","i5","i6","i7","i8","i9","scary","desc","i10")
private val bikeTypes = arrayOf("-","City-/Trekking Bike","Road Racing Bike","E-Bike","Liegerad","Lastenrad","Tandembike","Mountainbik","Sonstiges")
private val phoneLocations = arrayOf("Hosentasche","Lenker","Jackentasche","Hand","Fahrradkorb","Rucksack/Tasche","Sonstiges")
private val incidentTypes = arrayOf("Nichts","Zu dichtes Überholen","Ein- oder ausparkendes Fahrzeug","Beinahe-Abbiegeunfall","Entgegenkommender Verkehrsteilnehmer","Zu dichtes Auffahren","Beinahe-Dooring","Hindernis ausweichen (z.B. Hund)","Sonstiges")
private val participants = arrayOf("Bus","Fahrrad","Fußgänger","Lieferwagen","LKW","Motorrad","PKW","Taxi","Sonstiges","E-Scooter")
fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    val cla = mainBody { ArgParser(args).parseInto(::Conf) }
    val incidentsInfo: MutableList<String> = mutableListOf()
    val ridesInfo: MutableMap<String, MutableList<String>> = mutableMapOf()
    logger.info("creating " + cla.outputDir.absolutePath +  File.separator + cla.region + "-incidents.json and " + cla.outputDir.absolutePath +  File.separator + cla.region + "-incidents.html")
    var bboxList: ArrayList<Double>? = null
    if (cla.bbox != "0.0,0.0,0.0,0.0") {
        bboxList = ArrayList()
        cla.bbox.split(",").forEach { it ->
            bboxList.add(it.toDouble())
        }
    }
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString().lowercase() == it.name.lowercase() || (cla.region.toString() == "all" && !it.name.endsWith(".zip") && !it.name.contains("_")) && !it.name.equals("Regions")) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    val thisIncidentsInfo: MutableList<String> = getIncidents(path.absolutePath)
                    val thisRidesInfo: MutableList<String> = getGPSPoints(path.absolutePath, bboxList)
                    incidentsInfo.addAll(thisIncidentsInfo)
                    if (thisRidesInfo.size > 0) {
                        ridesInfo[path.name] = thisRidesInfo
                    }
                }
            }
        }
    }
    printGeoJson(incidentsInfo, ridesInfo, cla.outputDir, cla.region.toString())
    val endTime = System.currentTimeMillis()
    logger.info("Done. Number of Incidents: " + incidentsInfo.size + ". Execution took " + ((endTime - startTime)/1000) + " seconds.")
    cla.toMetaFile()
}
/**
 * Pretty prints a geoJson file where the incidents are written as points.
 */
fun printGeoJson(incidents: MutableList<String>, rides: MutableMap<String, MutableList<String>>, outputDir: File, region: String) {
    val featureCollection = FeatureCollection()
    var incidentsAsCsv = "lat,lon,ts,bike,childCheckBox,trailerCheckBox,pLoc,incident,i1,i2,i3,i4,i5,i6,i7,i8,i9,scary,desc,i10,region\n"
    incidents.forEach { incidentLine ->
        val elements: List<String>
        if (incidentLine.contains(",\"") && incidentLine.contains("\",")) {
            val parser = CSVParserBuilder().withSeparator(',').withQuoteChar('\"').build()
            val csvReader = CSVReaderBuilder(StringReader(incidentLine)).withSkipLines(0).withCSVParser(parser).build()
            elements = csvReader.readNext().toList()
        } else {
            elements = incidentLine.split(",")
        }
        var quotedIncidentLine = ""
        var commaPrefix = ""
        elements.forEachIndexed { index, s ->
            if (index == 18) {
                quotedIncidentLine += commaPrefix + "\"" + (s.replace(";komma;",",").replace(";linebreak;","\\n")).trim() + "\""
            } else {
                quotedIncidentLine += commaPrefix + (s)
            }
            commaPrefix = ","
        }
        incidentsAsCsv += (quotedIncidentLine + "\n")

        val point = Point((elements[1]).toDouble(),(elements[0]).toDouble())
        val propertiesMap = mutableMapOf<String,String>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val timestamp = if (elements[2].isNotEmpty()) sdf.format((Date((elements[2]).toLong()))) else 0
        val bikeType = if (elements[3].isNotEmpty()) bikeTypes[elements[3].toInt()] else "-"
        val childCheckBox = if (elements[4] == "1") "Ja" else "Nein"
        val trailerCheckBox = if (elements[5] == "1") "Ja" else "Nein"
        val pLoc = if (elements[6].isNotEmpty()) phoneLocations[elements[6].toInt()] else phoneLocations[0]
        val incident = if (elements[7].isNotEmpty()) incidentTypes[elements[7].toInt()] else incidentTypes[0]
        var prefix = ""
        var participantsText = "";
        elements.subList(8,17).forEachIndexed() { index, participant ->
            if (participant.isNotEmpty() && participant.toInt() == 1) {
                participantsText += prefix
                participantsText += participants[index]
                prefix = ", "
            }
        }
        val scary = if (elements[17] == "1") "Ja" else "Nein"
        val desc = "\""+ elements[18].replace(";komma;",",").replace(";linebreak;","\\n").trim() + "\""
        participantsText += if (elements[19] == "1") (prefix + participants[9]) else ""

        val thisRegion = if (elements.size > 20) {
            elements[20]
        } else {
            elements[19]
        }

        val rideName = if (elements.size > 21) {
            elements[21]
        } else {
            elements[20]
        }

        propertiesMap["date"] = timestamp.toString()
        propertiesMap["bikeType"] = bikeType
        propertiesMap["child"] = childCheckBox
        propertiesMap["trailer"] = trailerCheckBox
        propertiesMap["pLoc"] = pLoc
        propertiesMap["incident"] = incident
        propertiesMap["participant"] = participantsText
        propertiesMap["scary"] = scary
        propertiesMap["descr"] = desc
        propertiesMap["region"] = thisRegion
        propertiesMap["ride"] = rideName

        featureCollection.add(Feature().apply {
            geometry = point
            properties = propertiesMap as Map<String, Any>?
        })
    }

    rides.forEach { rideLine ->
        val lineString = LineString()
        rideLine.value.forEach { entry ->
            lineString.add(LngLatAlt(entry.split(",")[0].toDouble(),entry.split(",")[1].toDouble()))
        }
        val propertiesMap = mutableMapOf<String,String>()
        propertiesMap["ride"] = rideLine.key
        featureCollection.add(Feature().apply {
            geometry = lineString
            properties = propertiesMap as Map<String, Any>?
        })
        /*val elements: List<String>

        if (rideLine.contains(",\"") && rideLine.contains("\",")) {
            val parser = CSVParserBuilder().withSeparator(',').withQuoteChar('\"').build()
            val csvReader = CSVReaderBuilder(StringReader(rideLine)).withSkipLines(0).withCSVParser(parser).build()
            elements = csvReader.readNext().toList()
            incidentsAsCsv += rideLine + "\n"
        } else {
            elements = rideLine.split(",")
            val cleanedIncidentLine = rideLine
            incidentsAsCsv += (cleanedIncidentLine + "\n")
        }

        val point = LineString()
        val propertiesMap = mutableMapOf<String,String>()
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val timestamp = if (elements[2].isNotEmpty()) sdf.format((Date((elements[2]).toLong()))) else 0
        val bikeType = if (elements[3].isNotEmpty()) bikeTypes[elements[3].toInt()] else "-"
        val childCheckBox = if (elements[4] == "1") "Ja" else "Nein"
        val trailerCheckBox = if (elements[5] == "1") "Ja" else "Nein"
        val pLoc = if (elements[6].isNotEmpty()) phoneLocations[elements[6].toInt()] else phoneLocations[0]
        val incident = if (elements[7].isNotEmpty()) incidentTypes[elements[7].toInt()] else incidentTypes[0]
        var prefix = ""
        var participantsText = "";
        elements.subList(8,17).forEachIndexed() { index, participant ->
            if (participant.isNotEmpty() && participant.toInt() == 1) {
                participantsText += prefix
                participantsText += participants[index]
                prefix = ", "
            }
        }
        val scary = if (elements[17] == "1") "Ja" else "Nein"
        val desc = elements[18].replace(";komma;",",").replace(";linebreak;","\\n")

        participantsText += if (elements[19] == "1") (prefix + participants[9]) else ""

        val thisRegion = if (elements.size > 20) {
            elements[20]
        } else {
            elements[19]
        }

        val rideName = if (elements.size > 21) {
            elements[21]
        } else {
            elements[20]
        }

        propertiesMap["date"] = timestamp.toString()
        propertiesMap["bikeType"] = bikeType
        propertiesMap["child"] = childCheckBox
        propertiesMap["trailer"] = trailerCheckBox
        propertiesMap["pLoc"] = pLoc
        propertiesMap["incident"] = incident
        propertiesMap["participant"] = participantsText
        propertiesMap["scary"] = scary
        propertiesMap["descr"] = desc
        propertiesMap["region"] = thisRegion
        propertiesMap["ride"] = rideName

        featureCollection.add(Feature().apply {
            geometry = point
            properties = propertiesMap as Map<String, Any>?
        })*/
    }
    val pathToJSON = outputDir.absolutePath +  File.separator + region + "-incidents.json"
    val pathToCSV = outputDir.absolutePath +  File.separator + region + "-incidents.csv"
    val pathToZIP = outputDir.absolutePath +  File.separator + region + "-incidents.zip"
    ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get(pathToJSON).toFile(), featureCollection)
    Paths.get(pathToCSV).toFile().writeText(incidentsAsCsv)
    val files: Array<String> = arrayOf(pathToJSON, pathToCSV)
    val fos = FileOutputStream(pathToZIP)

    val zipOut = ZipOutputStream(fos)
    for (srcFile in files) {
        val fileToZip = File(srcFile)
        val fis = FileInputStream(fileToZip)
        val zipEntry = ZipEntry(fileToZip.name)
        zipOut.putNextEntry(zipEntry)
        val bytes = ByteArray(1024)
        var length: Int
        while (fis.read(bytes).also { length = it } >= 0) {
            zipOut.write(bytes, 0, length)
        }
        fis.close()
    }
    zipOut.close()
    fos.close()
}

/**
 * Traverses a given ride and returns a List of incidents without incident key
 * @param path - The absolute path to the ride file
 * @return incidents without keys of given ride
 */
fun getIncidents(path: String): MutableList<String> {
    val region = path.split("Regions${File.separator}")[1].split("${File.separator}Rides")[0]
    val result: MutableList<String> = mutableListOf() // contains the result
    val inputStream: InputStream = File(path).inputStream() // read the file
    var incidentPart = false // set to true after the incident header "key,lat,lon,..." is passed
    // iterate through the ride file
    inputStream.bufferedReader().useLines { lines -> lines.forEach { line ->
        if (line.isEmpty()) {
            return result
        }
        // if incident part is reached...
        if (incidentPart) {
            // split the array by comma
            var lineArray = line.split(",")
            // omit the first element (key)
            lineArray = lineArray.subList(1, lineArray.size)
            var thisIncident = ""
            var prefix = ""
            // add the elements to result
            lineArray.forEach { element ->
                thisIncident += (prefix)
                thisIncident += (element)
                prefix = ","
            }
            if(!thisIncident.startsWith(",,,") && lineArray[7].isNotEmpty() && lineArray[7].toInt() != -5  && lineArray[7].toInt() != 0) {
                thisIncident = correctTimeStamp(thisIncident, path)
                result.add("$thisIncident,$region,${File(path).name}")
            } // set incidentPart to true, if header "key,lat,lon,..." is reached
        } else if(line.startsWith("key,lat,lon")) {
            incidentPart = true
        }
    } }
    return result
}

/**
 * Traverses a given ride and returns a List of GPS and timestamps (lat,lon,ts) or null, if given bbox is not null and
 * ride does not go through bbox
 * @param path - The absolute path to the ride file
 * @param bbox - (optional) if given, checks if the ride goes through the bbox.
 * @return gps and timestamp of given ride
 */
fun getGPSPoints(path: String, bbox: ArrayList<Double>?): MutableList<String> {
    var result: MutableList<String> = mutableListOf() // contains the result
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
                result.add(("$lat,$lon"))
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
        result = mutableListOf()
    }
    return result
}

fun inBoundingBox(blLat: Double, blLon: Double, trLat: Double, trLon:Double, pLat:Double, pLon:Double): Boolean {
    // in case longitude 180 is inside the box

    return pLat in blLat..trLat && pLon in blLon..trLon
}

/**
 * Retrieves the correct timestamp of old manually added incidents, which had 1337 as timestamp
 */
fun correctTimeStamp(thisIncident: String, path: String): String {
    val parser = CSVParserBuilder().withSeparator(',').withIgnoreQuotations(true).build()
    // val csvReader = CSVReaderBuilder(StringReader(thisIncident.replace("“","\""))).withSkipLines(0).withCSVParser(parser).build()
    val csvReader = CSVReaderBuilder(StringReader(thisIncident)).withSkipLines(0).withCSVParser(parser).build()

    val elements: MutableList<String> = csvReader.readNext().toList().toMutableList()
    if (elements[2] != "1337") {
        return thisIncident
    } else {
        val lat = elements[0] // latitude to look for in the ride part
        val lon = elements[1] // longitude to look for in the ride part
        val inputStream: InputStream = File(path).inputStream() // read the file
        var ridePart = false // set to true after the ride header "lat,lon,X,..." is passed
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.isNotEmpty()) {
                    if (ridePart && !line.startsWith(",,")) {
                        val thisLine = line.split(",")
                        val thisLat = thisLine[0]
                        val thisLon = thisLine[1]
                        if (thisLat == lat && thisLon == lon) {
                            elements[2] = thisLine[5]
                        }
                    } else if (line.startsWith("lat,lon,X")) {
                        ridePart = true
                    }

                }
            }
        }
        return elements.joinToString(separator = ",")
    }
}
