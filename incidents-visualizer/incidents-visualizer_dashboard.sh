#!/bin/bash

SIMRAROOT=/e/tubCloud/Regions
OSMDIR=/c/Repo/osmPreparation/output_data
OUTPUTDIR=/c/Repo/scripts/incidents-visualizer/output_data
JARDIR=/c/Repo/scripts/incidents-visualizer/incidents-visualizer.jar
LISTDIR=/c/Repo/scripts/incidents-visualizer/simRa_regions_coords_ID.config

java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Alzey
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Augsburg
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Berlin
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Bern
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Bielefeld
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Bruchsal
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Bruehl
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Darmstadt
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Düsseldorf
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Eichwalde
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Frankfurt
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Freiburg
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Friedrichshafen
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Hannover
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Kaiserslautern
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Karlsruhe
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Koblenz
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Landau
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Ahrweiler #Landkreis start
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Breisgau-Hochschwarzwald
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Konstanz
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Saarlouis
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Sigmaringen
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Tuttlingen
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Tübingen
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Vulkaneifel #Landkreis end
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Leipzig
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Mainz
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Mannheim
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r München
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Nuernberg
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Odenwald
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Ortenau
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Pforzheim
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Rastatt
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Dresden #Region Dresden
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Stuttgart #Region Stuttgart
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Ruhrgebiet
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Saarbrücken
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Trier
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Ulm
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Weimar
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Wetterau
java -jar $JARDIR -s $SIMRAROOT --osmDir $OSMDIR -o $OUTPUTDIR -l $LISTDIR -r Wuppertal
