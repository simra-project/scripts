package main

class HtmlTemplate (region: String) {
    var html = "<!doctype html>\n" +
            "<html>\n" +
            " <head> \n" +
            "  <meta charset=\"utf-8\"> \n" +
            "  <title>SimRa $region Incidents</title> \n" +
            "  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.0.3/dist/leaflet.css\"> \n" +
            "  <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" +
            "  <script src=\"https://unpkg.com/leaflet@1.0.3/dist/leaflet.js\"></script> \n" +
            "  <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js\"></script> \n" +
            "  <script src=\"https://unpkg.com/leaflet.markercluster@1.4.1/dist/leaflet.markercluster.js\"></script> \n" +
            "  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.css\"> \n" +
            "  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.Default.css\"> \n" +
            "  <style>  \n" +
            "\t\t\tbody {\n" +
            "\t\t\t\tpadding: 0;\n" +
            "\t\t\t\tmargin: 0;\n" +
            "\t\t\t}\n" +
            "\t\t\thtml, body, #map {\n" +
            "\t\t\t\theight: 100%;\n" +
            "\t\t\t\twidth: 100%;\n" +
            "\t\t\t}\n" +
            "\t\t\t#download {\n" +
            "\t\t\t\tborder: 2px solid rgba(0,0,0,0.2);\n" +
            "\t\t\t\tbackground-clip: padding-box;\n" +
            "\t\t\t\tbackground-color: DodgerBlue;\n" +
            "\t\t\t\tborder: none;\n" +
            "\t\t\t\tcolor: white;\n" +
            "\t\t\t\theight: 44px;\n" +
            "\t\t\t\twidth: 44px;\n" +
            "\t\t\t\tcursor: pointer;\n" +
            "\t\t\t\tfont-size: 20px;\n" +
            "\t\t\t\tdisplay: block;\n" +
            "\t\t\t\tposition: absolute;\n" +
            "\t\t\t\ttop: 80px;\n" +
            "\t\t\t\tright: 12px;\n" +
            "\t\t\t\tz-index: 500;\n" +
            "\t\t\t}\n" +
            "\t\t\t#download:hover {\n" +
            "\t\t\t\tbackground-color: RoyalBlue;\n" +
            "\t\t\t}\n" +
            "  </style> \n" +
            " </head> \n" +
            " <body> \n" +
            "  <div id=\"map\">\n" +
            "  <button type=\"submit\" id=\"download\" onclick=\"window.open('${region}_incidents.zip')\" class=\"download\"><i class=\"fa fa-download\"></i></button>\n" +
            "  </div> \n" +
            "  <script>\n" +
            "\t\t\tvar url = '${region}_incidents.json';\n" +
            "\t\t\t\n" +
            "\t\t\tvar map = L.map('map').setView([51.51372076,7.51593812], 5); \n" +
            "\t\t\t\n" +
            "\t\t\tvar osm=new L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png',{ \n" +
            "\t\t\t\t\t\tattribution: '&copy; <a href=\"http://osm.org/copyright\">OpenStreetMap</a> contributors'}).addTo(map);\n" +
            "\t\t\t\t\n" +
            "\t\t\t\t\n" +
            "\t\t\tvar normal_style = {\n" +
            "\t\t\t\t'weight': 3,\n" +
            "\t\t\t\t'opacity': 0.6,\n" +
            "\t\t\t\tcolor: '#000000'\n" +
            "\t\t\t}\n" +
            "\t\t\tvar highlight_style = {\n" +
            "\t\t\t\t'weight': 6,\n" +
            "\t\t\t\t'opacity': 1,\n" +
            "\t\t\t\tcolor: '#0000ff'\n" +
            "\t\t\t};\n" +
            "\t\t\t\n" +
            "\t\t\t// Set style function that sets fill color property\n" +
            "\t\t\tfunction style(feature) {\n" +
            "\t\t\t    return {\n" +
            "\t\t\t\t'weight': 3,\n" +
            "\t\t\t\t'opacity': 0.6,\n" +
            "\t\t\t\tcolor: '#000000'\n" +
            "\t\t\t    };\n" +
            "\t\t\t}\n" +
            "\t\t\t\n" +
            "\t\t\tfunction forEachFeature(feature, layer) {\n" +
            "\t\t\t\n" +
            "\t\t\t\tvar popupContent = '<p><b>Datum: </b>' + feature.properties.date\n" +
            "\t\t\t\t+ '<br /> <b>Fahrradtyp: </b>' + feature.properties.bikeType\n" +
            "\t\t\t\t+ '<br /> <b>Kind transportiert: </b>' + feature.properties.child\n" +
            "\t\t\t\t+ '<br /> <b>Anhänger dabei: </b>' + feature.properties.trailer\n" +
            "\t\t\t\t+ '<br /> <b>Handyort: </b>' + feature.properties.pLoc\n" +
            "\t\t\t\t+ '<br /> <b>Beinaheunfalltyp: </b>' + feature.properties.incident\n" +
            "\t\t\t\t+ '<br /> <b>Beteiligte Verkehrsteilnehmer: </b>' + feature.properties.participant\n" +
            "\t\t\t\t+ '<br /> <b>Diese Erfahrung war beängstigend: </b>' + feature.properties.scary\n" +
            "\t\t\t\t+ '<br /> <b>Beschreibung: </b>' + feature.properties.descr\n" +
            "\t\t\t\t+ '</p>';\n" +
            "\t\t\t\n" +
            "\t\t\t\tlayer.bindPopup(popupContent);\n" +
            "\t\t\t\tlayer.on('click', function () {\n" +
            "\t\t\t\t});\n" +
            "\t\t\t}\n" +
            "\t\t\t\t\n" +
            "\t\t\t// Null variable that will hold layer\n" +
            "\t\t\tvar redIcon = new L.Icon({\n" +
            "\t\t\t       iconUrl: './marker-icon-2x-red.png',\n" +
            "\t\t\t       iconSize: [25, 41],\n" +
            "\t\t\t       iconAnchor: [12, 41],\n" +
            "\t\t\t       popupAnchor: [1, -34],\n" +
            "\t\t\t       shadowSize: [41, 41]\n" +
            "\t\t\t});\n" +
            "\t\t\t\n" +
            "\t\t\tvar blueIcon = new L.Icon({\n" +
            "\t\t\t       iconUrl: './marker-icon-2x-blue.png',\n" +
            "\t\t\t       iconSize: [25, 41],\n" +
            "\t\t\t       iconAnchor: [12, 41],\n" +
            "\t\t\t       popupAnchor: [1, -34],\n" +
            "\t\t\t       shadowSize: [41, 41]\n" +
            "\t\t\t});\n" +
            "\t\t\t\n" +
            "\t\t\tvar incidentLayer;\n" +
            "\t\t\tvar markers = L.markerClusterGroup();\n" +
            "\t\t\t\n" +
            "\t\t\t\$.getJSON(url, function(data) {\n" +
            "\t\t\t\tincidentLayer = L.geoJson(data, {\n" +
            "\t\t\t\t\tpointToLayer: function(feature, latlng) {\n" +
            "\t\t\t\t\t\tif (feature.properties.scary === \"Ja\") {\n" +
            "\t\t\t\t\t\t\treturn L.marker(latlng, {\n" +
            "\t\t\t\t\t\t\t\ticon: redIcon\n" +
            "\t\t\t\t\t\t\t});\n" +
            "\t\t\t\t\t\t} else {\n" +
            "\t\t\t\t\t\t\treturn L.marker(latlng, {\n" +
            "\t\t\t\t\t\t\t\ticon: blueIcon\n" +
            "\t\t\t\t\t\t\t});\n" +
            "\t\t\t\t\t\t}\n" +
            "\t\t\t\t\t},\n" +
            "\t\t\t\t\tonEachFeature: forEachFeature, style: style});\n" +
            "\t\t\t       // incidentLayer.addData(data);\n" +
            "\t\t   }).done(function() {\n" +
            "\t\t\t\tmarkers.addLayer(incidentLayer);\n" +
            "\t\t\t\tmarkers.addTo(map);\n" +
            "\t\t\t});\n" +
            "\t\t\t\n" +
            "\t\t\t\n" +
            "\t\t\t// for Layer Control\t\n" +
            "\t\t\tvar baseMaps = {\n" +
            "\t\t\t    \"Open Street Map\": osm  \t\n" +
            "\t\t\t};\n" +
            "\t\t\t\n" +
            "\t\t\tvar overlayMaps = {\n" +
            "\t\t\t    \"Incidents\":markers\n" +
            "\t\t\t};\t\n" +
            "\t\t\t\n" +
            "\t\t\t\t\n" +
            "\t\t\t//Add layer control\n" +
            "\t\t\tL.control.layers(baseMaps, overlayMaps).addTo(map);\n" +
            "  </script>   \n" +
            " </body>\n" +
            "</html>"
}