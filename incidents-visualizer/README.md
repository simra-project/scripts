# SimRa MapJson Generator

This project creates geojson files that are visualized on the [SimRa project website](https://github.com/simra-project/simra-project.github.io/tree/master).
Furthermore, this project creates a csv file containing the incidents in the same format as the [SimRa app](https://github.com/simra-project/simra-android)
In essence, data on incidents in a SimRa region gathered by users of the SimRa app is mapped onto specific areas of the respective region, producing data to be displayed on a Leaflet map.

## Required toolchain

- Git
- Maven
- Kotlin (1.5.20 or higher)

## Required data

For a specific SimRa region, you will need incident data (obtained from https://github.com/simra-project/dataset), only Berlin thus far, but other data upon request (see [instructions in bottom section](https://simra-project.github.io/mitmachen.html))

## How to use

The entry point for running this project is `src/kotlin/main/Main.kt`. 

A number of input parameters can be specified, with defaults being provided for each.
- `-s`/`--simraRoot`: the directory from which the incident data will be read. `incidents-visualizer/data` by default.
- `-r`/`--region`: the region the computations shall be carried out for. `all` by default.
- `-l`/`--regionList`: the simRa_regions_coords_ID.config containing the SimRa Regions in a list. `incidents-visualizer/simRa_regions_coords_ID.config` by default.
- `-o`/`--outputDir: the directory into which output data will be written, i.e. `{region}.json` (the to-be-visualized data) as well as files containing meta-information (`{region}-meta.json`, `{region_all-meta.json}`) such as utilized cut-off values for input parameters listed below.
Default: `osmColoring/output_data`.
- `--osmDir`: the directory from which the meta data written by `osmPreparation` will be read. Default: `incidents-visualizer/osm_data`.
- `-h`/`--help`: print help message and exit.


Output data (written to `outputDir`):
- `{region}-incidents.json`: the GeoJson file containing a region's incidents as clickable markers to be plotted on a Leaflet-map.
- `{region}-incidents.csv`: the csv file containing a region's incidents, each line an incident. This is available for download as open data. Further information about the format can be found in the [dataset description](https://github.com/simra-project/dataset)
- `{region}-incidents-meta.json`: as the title suggests, this file contains meta information such as the date, centroid and zoom level for the to-be-generated map, and a short description.
- `{region}-incidents.zip`: the zip file containing the json and csv

## Viewing the result on a map

This project contains the [SimRa project website](https://simra-project.github.io/) as a submodule for the purpose of visualizing the resultant GeoJson data on a map.
Analogous to the [incidents map of Berlin already published](https://simra-project.github.io/incidents.html?region=berlin), users can view the incidents for any region whose data they have access to.

This is how you can use the project website's code to plot your results:
- The SimRa project website is embedded in this project as a sub-module (perhaps you've already noticed the empty `simra-project.github.io`directory). In order to activate
the submodule, execute the following commands:
```
git submodule init
git submodule update
```
- Move the files you've generated using this project (`{region}-incidents.json` and `{region}-incidents-meta.json`) into `simra-project.github.io/incidents`.
- In `simra-project.github.io/resources/map-region.js`, add the respective region to the `switch (params.get("region"))`-block. Example:
```
case "augsburg":
        region = "region/Augsburg.json";
        regionMeta = "region/Augsburg-meta.json";
        incidents = "incidents/Augsburg-incidents.json"
        incidentsMeta = "incidents/Augsburg-incidents-meta.json"
        break;
``` 
- Start a local HTTP server (inside the top level directory or the `simra-project.github.io` subdirectory, doesn't really matter): `python3 -m http.server`. In your browser, enter `localhost:8000`. 
The directory structure will appear. If you're not already inside it, navigate into the directory `simra-project.github.io`. Select `incidents.html`.
In the browser address bar, add the respective URL search parameter for the region in question. Example: 
```
http://localhost:8000/map.html?region=augsburg
```
- That's it! You should be able to view your results on a map.
