import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

class Ride(val originalFile: File) {

    init {
        check(originalFile.exists()) { "Ride file ${originalFile.absolutePath} does not exist" }
        check(originalFile.absolutePath.split("${File.separator}Rides${File.separator}")[1].split(File.separator).size == 1) {
            "Ride file ${originalFile.absolutePath} seems not to be located wihtin the old directory structure."
        }
    }

    private val creationTime = Files.getAttribute(originalFile.toPath(), "creationTime") as FileTime
    val date = creationTime.toInstant().atZone(ZoneId.of("Europe/Berlin")).apply {
        check(this.year <= LocalDateTime.now().year) { "Year must be smaller than/equal to than the current year"}
        check(this.year >= 2018) { "Year must be larger than/equal to 2018"}

        check(this.month.value <= 12) { "Month must be smaller smaller than/equal to 12"}
        check(this.month.value >= 1) { "Month must be larger than/equal to 1"}
    }

    fun getTargetFilePath(): String {
        val split = originalFile.absolutePath.split("${File.separator}Rides${File.separator}")
        return "${split[0]}${File.separator}Rides${File.separator}${date.year}${File.separator}${date.month.twoDigits()}${File.separator}${split[1]}"
    }

    fun Month.twoDigits(): String {
        return String.format("%02d", this.value)
    }

}