import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val cla = mainBody { ArgParser(args).parseInto(::CommandLineArguments) }
    var avgSpeedAll = 0.0
    var numberOfSpeedsAll = 0
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString().lowercase() == it.name.lowercase() || (cla.region.toString() == "all" && !it.name.endsWith(".zip") && !it.name.contains("_")) && !it.name.equals("Regions")) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    var avgSpeed = 0.0
                    var numberOfSpeeds = 0
                    var incidentsAsCsv = "latitude,longitude,timestamp\n"
                    // get ride file
                    val inputStream: InputStream = path.inputStream() // read the file
                    var ridePart = false // set to true after the ride header "lat,lon,X,Y,Z" is passed
                    // iterate through the ride file
                    inputStream.bufferedReader().useLines { lines ->
                        var lastLat = -5000.0
                        var lastLon = -5000.0
                        var lastTS = 0L
                        lines.forEach {
                            // if ride part is reached...
                            if (ridePart) {
                                // split the array by comma
                                val lineArray = it.split(",")
                                // and if the line is a gps line, calculate speed and add to results
                                if (lineArray[0].isNotEmpty()) {
                                    if (lastLat == -5000.0) {
                                        lastLat = lineArray[0].toDouble()
                                        lastLon = lineArray[1].toDouble()
                                        lastTS = lineArray[5].toLong()
                                        incidentsAsCsv += "${lineArray[0]},${lineArray[1]},${lineArray[5]},0\n"
                                    } else {
                                        val distanceToLastInMeters = Location.distanceTo(lastLat,lastLon, lineArray[0].toDouble(), lineArray[1].toDouble())
                                        val durationToLastInSeconds = (lineArray[5].toLong() - lastTS) / 1000
                                        val currentSpeedMS = calculateSpeedMS(distanceToLastInMeters, durationToLastInSeconds)
                                        incidentsAsCsv += "${lineArray[0]},${lineArray[1]},${lineArray[5]},$currentSpeedMS\n"
                                        if (currentSpeedMS > 0) {
                                            avgSpeed += currentSpeedMS
                                            numberOfSpeeds++
                                            avgSpeedAll += currentSpeedMS
                                            numberOfSpeedsAll++
                                            println(it)
                                        }

                                        if(avgSpeedAll.toString() == "NaN") {
                                            println("distanceToLastInMeters:$distanceToLastInMeters seconds:${(lineArray[5].toLong() - lastTS) / 1000}")
                                            println("currentSpeed: $currentSpeedMS name: ${path.name}")
                                            exitProcess(1)
                                        }
                                    }
                                }
                            }
                            // set ridePart to true, if header "lat,lon,X,Y,Z" is reached
                            if (it.startsWith("lat,lon,X,Y,Z")) {
                                ridePart = true
                            }
                        }
                    }
                    // println("ride: ${path.name} avg speed: ${avgSpeed/numberOfSpeeds}m/s")
                    // write output
                    Paths.get(cla.outputDir + File.separator + path.name + "_" + (avgSpeed/numberOfSpeeds).toString().take(3) + ".csv").toFile().writeText(incidentsAsCsv)
                }
            }
        }
    }
    println("avgSpeedAll:$avgSpeedAll")
    println("numberOfSpeedsAll:$numberOfSpeedsAll")
    println("avg speed all: ${avgSpeedAll/numberOfSpeedsAll}m/s")
}

fun calculateSpeedMS(distanceToLastInMeters: Double, durationToLastInSeconds: Long): Double {
    return distanceToLastInMeters / durationToLastInSeconds
}
