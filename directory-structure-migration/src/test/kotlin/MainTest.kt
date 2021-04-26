import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class MainTest {

    val dataOld = File("src/test/resources/data")
    val dataNew = File("src/test/resources/data_new")

    @BeforeEach
    fun prepareSecondDir() {
        check(dataNew.exists()) { "Please copy the data directory to data_new before running the test."}
    }

    @Test
    fun runFullTest() {
        val rideFiles = getRideFiles(dataNew)
        val mapping = createFileTargetMapping(rideFiles)
        print(mapping)
        moveFiles(mapping)
    }


}