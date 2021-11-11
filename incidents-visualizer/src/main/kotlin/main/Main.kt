package main

import com.fasterxml.jackson.databind.ObjectMapper
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.apache.logging.log4j.LogManager
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.Point
import org.jsoup.Jsoup
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


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
    logger.info("creating " + cla.outputDir.absolutePath +  File.separator + cla.region + "_incidents.json and " + cla.outputDir.absolutePath +  File.separator + cla.region + "_incidents.html")
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString() == it.name || (cla.region.toString() == "all") && !it.name.endsWith(".zip") && !it.name.contains("_")) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    val thisIncidentsInfo: MutableList<String> = getIncidents(path.absolutePath)
                    incidentsInfo.addAll(thisIncidentsInfo)
                }
            }
        }
    }
    printGeoJson(incidentsInfo, cla.outputDir, cla.region.toString())
    printHtml(cla.outputDir, cla.region.toString())
    val endTime = System.currentTimeMillis()
    logger.info("Done. Number of Incidents: " + incidentsInfo.size + ". Execution took " + ((endTime - startTime)/1000) + " seconds.")
}

fun printHtml(outputDir: File, region: String) {

    val doc = Jsoup.parse(HtmlTemplate(region).html)

    Paths.get(outputDir.absolutePath +  File.separator + region + "_incidents.html").toFile().writeText(doc.toString())

}

/**
 * Pretty prints a geoJson file where the incidents are written as points.
 */
fun printGeoJson(incidents: MutableList<String>, outputDir: File, region: String) {
    val featureCollection = FeatureCollection()
    var incidentsAsCsv = "lat,lon,ts,bike,childCheckBox,trailerCheckBox,pLoc,incident,i1,i2,i3,i4,i5,i6,i7,i8,i9,scary,desc,i10,region\n"
    incidents.forEach { incidentLine ->
        val cleanedIncidentLine = incidentLine.replace(";komma;",",").replace(";linebreak;","\\n")
        incidentsAsCsv += (cleanedIncidentLine + "\n")
        val elements = cleanedIncidentLine.split(",")
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
        val desc = elements[18]
        participantsText += if (elements[19] == "1") (prefix + participants[9]) else ""
        // val thisRegion = elements[20]

        propertiesMap["date"] = timestamp.toString()
        propertiesMap["bikeType"] = bikeType
        propertiesMap["child"] = childCheckBox
        propertiesMap["trailer"] = trailerCheckBox
        propertiesMap["pLoc"] = pLoc
        propertiesMap["incident"] = incident
        propertiesMap["participant"] = participantsText
        propertiesMap["scary"] = scary
        propertiesMap["descr"] = desc
        // propertiesMap["region"] = thisRegion

        featureCollection.add(Feature().apply {
            geometry = point
            properties = propertiesMap as Map<String, Any>?
        })
    }
    val pathToJSON = outputDir.absolutePath +  File.separator + region + "_incidents.json"
    val pathToCSV = outputDir.absolutePath +  File.separator + region + "_incidents.csv"
    val pathToZIP = outputDir.absolutePath +  File.separator + region + "_incidents.zip"
    ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get(pathToJSON).toFile(), featureCollection)
    Paths.get(pathToCSV).toFile().writeText(incidentsAsCsv)
    val files: Array<String> = arrayOf(pathToJSON, pathToCSV)
    val fos = FileOutputStream(pathToZIP)
/*        val zos = ZipOutputStream(fos)
    files.forEach {
        val fileToZip = File(it)
        val fis = FileInputStream(fileToZip)
        val zipEntry = ZipEntry(fileToZip.name)
        zos.putNextEntry(zipEntry)

        val bytes = byteArrayOf()
        var length: Int

    }*/

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
    val region = path.split("Regions\\")[1].split("\\Rides")[0]
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
            if(!thisIncident.startsWith(",,,") && lineArray[7].isNotEmpty() && lineArray[7].toInt() != -5) {
                result.add(thisIncident + ",${region}")
            }
        }
        // set incidentPart to true, if header "key,lat,lon,..." is reached
        if(line.startsWith("key,lat,lon")) {
            incidentPart = true
        }
    } }
    return result
}