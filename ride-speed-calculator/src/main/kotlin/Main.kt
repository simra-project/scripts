import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.File
import java.io.InputStream
import java.nio.file.Paths

fun main(args: Array<String>) {
    val cla = mainBody { ArgParser(args).parseInto(::CommandLineArguments) }
    File(cla.simraRoot.toURI()).walk().maxDepth(1).forEach { it ->
        if(cla.region.toString().lowercase() == it.name.lowercase() || (cla.region.toString() == "all" && !it.name.endsWith(".zip") && !it.name.contains("_")) && !it.name.equals("Regions")) {
            File(it.toURI()).walk().forEach { path ->
                if(path.isFile && path.toString().contains("Rides") && path.name.startsWith("VM2_")) {
                    var incidentsAsCsv = "latitude,longitude,timestamp\n"
                    // get ride file
                    val inputStream: InputStream = path.inputStream() // read the file
                    var ridePart = false // set to true after the ride header "lat,lon,X,Y,Z" is passed
                    // iterate through the ride file
                    inputStream.bufferedReader().useLines { lines ->
                        lines.forEach {
                            // if ride part is reached...
                            if (ridePart) {
                                // split the array by comma
                                val lineArray = it.split(",")
                                // and if the line is a gps line, calculate speed and add to results
                                if (lineArray[0].isNotEmpty()) {
                                    incidentsAsCsv += "${lineArray[0]},${lineArray[1]},${lineArray[5]}\n"
                                }
                            }
                            // set ridePart to true, if header "lat,lon,X,Y,Z" is reached
                            if (it.startsWith("lat,lon,X,Y,Z")) {
                                ridePart = true
                            }
                        }
                    }
                    // write output
                    Paths.get(cla.outputDir + File.separator + path.name + ".csv").toFile().writeText(incidentsAsCsv)
                }
            }
        }
    }
}