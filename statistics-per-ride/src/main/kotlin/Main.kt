import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import kotlin.system.exitProcess

private val bikeTypes = arrayOf("-","City-/Trekking Bike","Road Racing Bike","E-Bike","Liegerad","Lastenrad","Tandembike","Mountainbike","Sonstiges")

fun main(args: Array<String>) {
    val cla = mainBody { ArgParser(args).parseInto(::CommandLineArguments) }
    var incidentsAsCsv = "Aufzeichnungsdatum,Wochentag,Aufzeichnungsuhrzeit,Fahrtlänge,Fahrtdauer,Fahrradtyp,Kindertransport,Anzahl der Beinaheunfälle,davon beängstigend\n"
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString().lowercase() == it.name.lowercase() || (cla.region.toString() == "all" && !it.name.endsWith(".zip") && !it.name.contains("_")) && !it.name.equals("Regions")) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    var startTS = 0L
                    var endTS = 0L
                    var rideLength = BigDecimal(0) // km
                    var rideDuration = BigDecimal(0) // min
                    var bikeType = ""
                    var child = 0
                    var numberOfNMI = 0
                    var numberOfScaryNMI = 0
                    // get ride file
                    val inputStream: InputStream = path.inputStream() // read the file
                    var ridePart = false // set to true after the ride header "lat,lon,X,Y,Z" is passed
                    var incidentPart = false // set to true after the incident header "key,lat,lon,..." is passed
                    // iterate through the ride file
                    inputStream.bufferedReader().useLines { lines ->
                        var lastLat = -5000.0
                        var lastLon = -5000.0
                        lines.forEach lines@ {
                            // if ride part is reached...
                            if (ridePart) {
                                // split the array by comma
                                val ridePartLineArray = it.split(",")
                                if (ridePartLineArray[0].isNotEmpty()) {
                                    // if start timestamp is not set yet, set it
                                    if (startTS == 0L) {
                                        startTS = ridePartLineArray[5].toLong()
                                    }
                                    // set last lat and lon if they are not set yet, else update the distance
                                    if (lastLat == -5000.0) {
                                        lastLat = ridePartLineArray[0].toDouble()
                                        lastLon = ridePartLineArray[1].toDouble()
                                    } else {
                                        val distanceToLastInMeters = Location.distanceTo(lastLat,lastLon, ridePartLineArray[0].toDouble(), ridePartLineArray[1].toDouble())
                                        rideLength = rideLength.add(BigDecimal(distanceToLastInMeters))
                                        lastLat = ridePartLineArray[0].toDouble()
                                        lastLon = ridePartLineArray[1].toDouble()
                                    }
                                    // the timestamp of the last line is the end timestamp
                                    endTS = ridePartLineArray[5].toLong()
                                }
                                // set ridePart to true, if header "lat,lon,X,Y,Z" is reached
                            } else if (it.startsWith("lat,lon,X,Y,Z")) {
                                ridePart = true
                            } else // if incident part is reached...
                                if (incidentPart) {
                                    // split the array by comma
                                    val incidentPartLineArray = it.split(",")

                                    if(it.isEmpty()) {
                                        incidentPart = false
                                        return@lines
                                    }
                                    // set bikeType and whether a child is transported, if unknown yet
                                    if (bikeType == "") {
                                        bikeType = if (incidentPartLineArray[4].isNotEmpty()) bikeTypes[incidentPartLineArray[4].toInt()] else "-"
                                        child = if (incidentPartLineArray[5].isNotEmpty()) incidentPartLineArray[5].toInt() else 0

                                    }
                                    // if incident ist not -5 (pseudo incident), update the incident counters
                                    if (!it.startsWith(",,,") ) {
                                        numberOfNMI++
                                        numberOfScaryNMI += incidentPartLineArray[18].toInt()
                                    }
                                // set incidentPart to true, if header "key,lat,lon,..." is reached
                                } else if(it.startsWith("key,lat,lon")) {
                                    incidentPart = true
                                }
                        }
                    }
                    val recordDate = Instant.ofEpochMilli(startTS).atZone(ZoneId.systemDefault()).toLocalDate()
                    val recordWeekDay = recordDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.GERMAN)
                    val recordTime = Instant.ofEpochMilli(startTS).atZone(ZoneId.systemDefault()).toLocalTime()
                    rideDuration = (BigDecimal(endTS).subtract(BigDecimal(startTS))).divide(BigDecimal(1000),2,RoundingMode.HALF_UP).divide(BigDecimal(60),2,RoundingMode.HALF_UP)
                    rideLength = rideLength.divide(BigDecimal(1000),2,RoundingMode.HALF_UP)
                    incidentsAsCsv += "$recordDate,$recordWeekDay,$recordTime,${rideLength.toPlainString()},${rideDuration.toPlainString()},$bikeType,$child,$numberOfNMI,$numberOfScaryNMI\n"
                }
            }
        }
    }
    // write output
    Paths.get(cla.outputDir + File.separator + cla.region + "_statistics.csv").toFile().writeText(incidentsAsCsv)
}

