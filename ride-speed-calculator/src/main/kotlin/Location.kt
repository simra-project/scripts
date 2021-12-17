import org.gavaghan.geodesy.Ellipsoid
import org.gavaghan.geodesy.GeodeticCalculator
import org.gavaghan.geodesy.GlobalPosition

class Location(private val latitude: Double, private val longitude: Double) {

    fun distanceTo(previousLocation:Location):Double{
        val pointA = GlobalPosition(latitude, longitude, 0.0)
        val pointB = GlobalPosition(previousLocation.latitude, previousLocation.longitude, 0.0)
        // Distance between Point A and Point B in meters
        return GeodeticCalculator().calculateGeodeticCurve(Ellipsoid.WGS84,pointB,pointA).ellipsoidalDistance
    }
}