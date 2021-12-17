import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

fun main(args: Array<String>) {
    val cla = mainBody { ArgParser(args).parseInto(::CommandLineArguments) }
    var incidentsAsCsv = "latitude,longitude,timestamp,speed\n"

    // get ride file
    val inputStream: InputStream = cla.ride.inputStream() // read the file
    var ridePart = false // set to true after the ride header "lat,lon,X,Y,Z" is passed
    var previousLocation: Location? = null
    var thisLocation:Location? = null
    var previousTS:Long = 0
    var thisTS:Long = 0
    // iterate through the ride file
    inputStream.bufferedReader().useLines { lines -> lines.forEach {
        // if ride part is reached...
        if (ridePart) {
            // split the array by comma
            val lineArray = it.split(",")
            // and if the line is a gps line, calculate speed and add to results
            if(lineArray[0].isNotEmpty()) {
                val lat = lineArray[0].toDouble()
                val lon = lineArray[1].toDouble()
                thisTS = lineArray[5].toLong()
                thisLocation = Location(lat,lon)
                if (previousLocation != null) {
                    val distance:Double = (thisLocation!!.distanceTo(previousLocation!!)) / 1000 // distance in km
                    val duration:Double = ((thisTS - previousTS).toDouble())/1000/60/60 // duration in h
                    val speed:Double = distance/duration // speed in km/h
                    // println("$lat,$lon,$distance,$duration,$thisTS,$speed")
                    incidentsAsCsv += "$lat,$lon,$thisTS,$speed\n"
                }
                previousTS = thisTS
                previousLocation = thisLocation
            }
        }
        // set ridePart to true, if header "lat,lon,X,Y,Z" is reached
        if(it.startsWith("lat,lon,X,Y,Z")) {
            ridePart = true
        }
    } }
    // write output
    Paths.get(cla.outputDir + File.separator + "output.csv").toFile().writeText(incidentsAsCsv)
}