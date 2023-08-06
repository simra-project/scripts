# SimRa Incidents and Rides Visualizer

This project creates html files that contain a leaflet map and the incidents and rides of a specified region.
## Required toolchain

- Git
- Maven
- Kotlin (1.5.20 or higher)

## Required data

For a specific SimRa region, you will need ride data (obtained from https://github.com/simra-project/dataset), only Berlin thus far, but other data upon request (see [instructions in bottom section](https://simra-project.github.io/mitmachen.html))

## How to use

The entry point for running this project is `src/kotlin/main/Main.kt`. 

A number of input parameters can be specified, with defaults being provided for each.
- `-s`/`--simraRoot`: the directory from which the ride data will be read. `incidents-and-rodes-visualizer/data` by default.
- `-o`/`--outputDir`: the directory in which the html file will be created. `incidents-and-rodes-visualizer/output_data/` by default.
- `-r`/`--region`: the region the computations shall be carried out for. `all` by default.
- `-h`/`--help`: print help message and exit.


Output data (written to `outputDir`):
- `{region}.html`: the html file containing a region's rides incidents as clickable markers plotted on a Leaflet-map.
- `bluemarker.png`: the icon for the non-scary incidents.
- `redmarker.png`: the icon for the scary incidents.


## Viewing the result on a map

- Just open the html `{region}.html`-file with an internet browser of your choice.
- That's it! You should be able to view your results on a map.
