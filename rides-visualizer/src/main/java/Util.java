import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {
    public static void main(String[] args) throws IOException {
        StringBuilder southEast = geoJsonHead();
        StringBuilder northEast = geoJsonHead();
        StringBuilder northWest = geoJsonHead();
        StringBuilder southWest = geoJsonHead();
        BigDecimal lat_max = new BigDecimal("52.574289");
        BigDecimal lat_min = new BigDecimal("52.465619");
        BigDecimal lon_max = new BigDecimal("13.516180");
        BigDecimal lon_min = new BigDecimal("13.227402");

        BigDecimal lat_threshold = lat_min.add(lat_max.subtract(lat_min).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN));
        BigDecimal lon_threshold = lon_min.add(lon_max.subtract(lon_min).divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN));

        System.out.println(lat_threshold + "," + lon_threshold);


        BufferedReader br = new BufferedReader(new FileReader("F:\\Berlin_names.geojson"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            if (line.startsWith("{ \"type\":")) {
                // System.out.println("line: " + line);
                JSONObject jsonObject = new JSONObject(line);
                JSONArray jsonArray = jsonObject.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0)/*.getJSONArray(0).get(0)*/;
                BigDecimal south = (BigDecimal) jsonArray.getJSONArray(0).get(1);
                BigDecimal north = (BigDecimal) jsonArray.getJSONArray(1).get(1);
                BigDecimal east = (BigDecimal) jsonArray.getJSONArray(0).get(0);
                BigDecimal west = (BigDecimal) jsonArray.getJSONArray(2).get(0);
                if (east.compareTo(lon_threshold) > 0) { // is in east
                    if (north.compareTo(lat_threshold) <= 0) { // is in south-east
                        addJSONArrayToString(southEast,jsonObject);
                    } else { // is in north-east
                        addJSONArrayToString(northEast,jsonObject);
                    }
                } else { // is in west
                    if (north.compareTo(lat_threshold) <= 0) { // is in south-west
                        addJSONArrayToString(southWest,jsonObject);
                    } else { // is in north-west
                        addJSONArrayToString(northWest,jsonObject);
                    }
                }
            }
        }
        addGeoJsonTail(southEast);
        addGeoJsonTail(northEast);
        addGeoJsonTail(northWest);
        addGeoJsonTail(southWest);

        System.out.println(southEast.length());
        System.out.println(northEast.length());
        System.out.println(northWest.length());
        System.out.println(southWest.length());

        writeGeoJsonToFile(southEast, "./Berlin_SE.geojson");
        writeGeoJsonToFile(northEast, "./Berlin_NE.geojson");
        writeGeoJsonToFile(northWest, "./Berlin_NW.geojson");
        writeGeoJsonToFile(southWest, "./Berlin_SW.geojson");

    }

    private static void writeGeoJsonToFile(StringBuilder sb, String path) throws IOException {
        File file = new File(path);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(sb);
        }
    }

    private static void addJSONArrayToString(StringBuilder sb, JSONObject jsonObject) {
        sb.append(System.lineSeparator()).append(jsonObject).append(",");
    }

    private static StringBuilder geoJsonHead() {
        return new StringBuilder("var segments = {").append(System.lineSeparator()).append("\t\"type\": \"FeatureCollection\",").append(System.lineSeparator()).append("\"features\": [");
    }

    private static void addGeoJsonTail(StringBuilder sb) {
        sb.append(System.lineSeparator()).append("]").append(System.lineSeparator()).append("};");
    }
}
