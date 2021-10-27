package main

class HtmlTemplate (region: String) {
    var html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "\t<title>SimRa " + region + " Rides</title>\n" +
            "\t<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.0.3/dist/leaflet.css\" />\n" +
            "\t<script src=\"https://unpkg.com/leaflet@1.0.3/dist/leaflet.js\"></script>\n" +
            "    <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js\"></script>\n" +
            "    \n" +
            "<style>  \n" +
            "   body {\n" +
            "        padding: 0;\n" +
            "        margin: 0;\n" +
            "    }\n" +
            "    html, body, #map {\n" +
            "        height: 100%;\n" +
            "        width: 100%;\n" +
            "    }\n" +
            "</style>\n" +
            "</head>\n" +
            "\n" +
            "<body>\n" +
            "<div id=\"map\" ></div>\n" +
            "</div>\n" +
            "\n" +
            "<script>\n" +
            "var url = '" + region + "_rides.json';\n" +
            "\n" +
            "var map = L.map('map').setView([51.51372076,7.51593812], 3); \n" +
            "\n" +
            "var osm=new L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png',{ \n" +
            "\t\t\tattribution: '&copy; <a href=\"http://osm.org/copyright\">OpenStreetMap</a> contributors'}).addTo(map);\n" +
            "\t\n" +
            "\t\n" +
            "var normal_style = {\n" +
            "\t'weight': 3,\n" +
            "\t'opacity': 0.6,\n" +
            "\tcolor: '#000000'\n" +
            "}\n" +
            "var highlight_style = {\n" +
            "\t'weight': 6,\n" +
            "\t'opacity': 1,\n" +
            "\tcolor: '#0000ff'\n" +
            "};\n" +
            "\n" +
            "// Set style function that sets fill color property\n" +
            "function style(feature) {\n" +
            "    return {\n" +
            "\t'weight': 3,\n" +
            "\t'opacity': 0.6,\n" +
            "\tcolor: '#000000'\n" +
            "    };\n" +
            "}\n" +
            "\n" +
            "function forEachFeature(feature, layer) {\n" +
            "\n" +
            "\tvar popupContent = \"<p><b>ride: </b>\" + feature.properties.ride + '</p>';\n" +
            "\n" +
            "\tlayer.bindPopup(popupContent);\n" +
            "\tlayer.on('mouseover', function () {\n" +
            "\t\tthis.setStyle(highlight_style);\n" +
            "\t});\n" +
            "\tlayer.on('mouseout', function () {\n" +
            "\t\tthis.setStyle(normal_style);\n" +
            "\t});\n" +
            "\tlayer.on('click', function () {\n" +
            "\t});\n" +
            "}\n" +
            "\t\n" +
            "// Null variable that will hold layer\n" +
            "var rideLayer = L.geoJson(null, {onEachFeature: forEachFeature, style: style});\n" +
            "\n" +
            "\t\$.getJSON(url, function(data) {\n" +
            "        rideLayer.addData(data);\n" +
            "    });\n" +
            "\n" +
            " rideLayer.addTo(map);\n" +
            "\n" +
            "// for Layer Control\t\n" +
            "var baseMaps = {\n" +
            "    \"Open Street Map\": osm  \t\n" +
            "};\n" +
            "\n" +
            "var overlayMaps = {\n" +
            "    \"Rides\":rideLayer\n" +
            "};\t\n" +
            "\n" +
            "\t\n" +
            "//Add layer control\n" +
            "L.control.layers(baseMaps, overlayMaps).addTo(map);\n" +
            "\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>"
}