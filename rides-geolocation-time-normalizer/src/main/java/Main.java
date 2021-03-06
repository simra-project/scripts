import main.CommandLineArguments;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * Creates a text file at --outputDir used for visualization purposes.
 *
 * The resulting text file contains lines with at least one gps location as a latitude, longitude pair.
 * Each pair represents a location of a SimRa user during a ride, where the first line contains the locations of all
 * included rides' first location. The second line, the second location of all rides etc.
 *
 * This can be used, for example, to create visualization where the locations of each line are projected on a map
 * one by one by a couple of seconds.
 *
 * See CommandLineArguments' help texts for further information on how to filter the rides to be included by region or date
 */
public class Main {
    static Logger logger = getLogger();

    public static void doGPSMapper(CommandLineArguments cla) {

        File[] regionFolders = getRelevantRegionFolders(cla.getSimraRoot(), cla.getRegions());

        File[] simRaRides = getRideFiles(regionFolders);

        ArrayList<ArrayList<String[]>> frameLines = calculateFrames(cla.getBoundingBox(), cla.getFromDate(), cla.getToDate(), simRaRides);

        writeFrames(frameLines, cla.getOutputDir(), cla.getRegions());
    }

    /**
     * Gets all relevant regions folders in SimRa root according to the regions parameter
     * @param simraRoot Folder containing all the region folders
     * @param regions Regions from which the rides shall be considered as Strings
     * @return
     */
    private static File[] getRelevantRegionFolders(File simraRoot, String regions) {
        if(regions.equals("all")) {
            return simraRoot.listFiles();
        } else {
            String[] regionsArray = regions.split(",");
            FilenameFilter ff = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    for (int i = 0; i < regionsArray.length; i++) {
                        if(name.equals(regionsArray[i])) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            return simraRoot.listFiles(ff);
        }
    }

    /**
     * Gets all ride files in the give Region folders
     * @param regionFolders Region Folders containing the ride files
     * @return
     */
    private static File[] getRideFiles(File[] regionFolders) {
        ArrayList<File> rideFiles = new ArrayList<>();
        for (int i = 0; i < regionFolders.length; i++) {
            File folder = regionFolders[i];
            if(folder.isDirectory()) {
                rideFiles.addAll(Arrays.asList(getRideFiles(Objects.requireNonNull(folder.listFiles()))));
            } else {
                if(folder.getAbsolutePath().contains("Rides")) {
                    rideFiles.add(folder);
                }
            }
            if (i % 1000 == 0) {
                logger.info("getRideFiles at step " + i + "/" + regionFolders.length);
            }
        }
        return rideFiles.toArray(new File[0]);
    }

    /**
     * returns an ArrayList<ArrayList<String[]>>, where each element consists of GPS locations of each ride in simRaRides, as if all these rides
     * started at the same time. Each line is a snapshot of all the rides in the boundingBox, ordered chronologically.
     * @param boundingBox Bounding box as {west,south,east,north} as longitude,latitude,longitude,latitude values, e.g, 13.021,52.3772,13.8081,52.6513 for Berlin
     * @param simRaRides SimRa ride files
     * @return
     */
    private static ArrayList<ArrayList<String[]>> calculateFrames(String boundingBox, String fromDate, String toDate, File[] simRaRides) {
        ArrayList<ArrayList<String[]>> frames = new ArrayList<>();
        for (int i = 0; i < simRaRides.length; i++) {
            File simRaRideFile = simRaRides[i];
            long lastModifiedTime;
            try {
                lastModifiedTime = Files.readAttributes(simRaRideFile.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (lastModifiedTime < Long.parseLong(fromDate) || lastModifiedTime > Long.parseLong(toDate)) {
                continue;
            }
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(simRaRideFile))) {
                String line;
                // skip to the data log part
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("lat,lon,X,Y,Z")) {
                        break;
                    }
                }
                int index = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.startsWith(",,")) {
                        String[] location = Arrays.copyOfRange(line.split(","),0,2);
                        if (isInBoundingBox(boundingBox, location)) {
                            if(index > frames.size()-1) {
                                frames.ensureCapacity(index);
                                for (int j = frames.size(); j < index; j++) {
                                    frames.add(new ArrayList<>());
                                }
                                ArrayList<String[]> newArrayList = new ArrayList<>();
                                newArrayList.add(location);
                                frames.add(newArrayList);
                            } else {
                                ArrayList<String[]> oldFrame = frames.get(index);
                                oldFrame.add(location);
                                frames.set(index,oldFrame);
                            }
                        }
                        index++;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (i%1000 == 0) {
                logger.info("calculateFrames at step " + i + "/" + simRaRides.length);
            }
        }
        return frames;
    }

    /**
     * Checks whether given location is in given bounding box
     * @param boundingBox Bounding box as {west,south,east,north} as longitude,latitude,longitude,latitude values, e.g, 13.021,52.3772,13.8081,52.6513 for Berlin
     * @param location GPS location as latitude,longitude
     * @return true, if given location is in give bounding box, false otherwise.
     */
    private static boolean isInBoundingBox(String boundingBox, String[] location) {
        if (boundingBox == null) {
            return true;
        } else {
            String[] westSouthEastNorth = boundingBox.split(",");
            double west, south, east, north, lat, lon;
            try{
                west = Double.parseDouble(westSouthEastNorth[0]);
                south = Double.parseDouble(westSouthEastNorth[1]);
                east = Double.parseDouble(westSouthEastNorth[2]);
                north = Double.parseDouble(westSouthEastNorth[3]);
                lat = Double.parseDouble(location[0]);
                lon = Double.parseDouble(location[1]);
            } catch (NumberFormatException e) {
                return false;
            }

            if (north >= lat && lat >= south){
                // check if bounding box is around the date line
                if (west <= east && west <= lon && lon <= east){
                    return true;
                } else if(west > east && (west <= lon || lon <= east)) {
                    return true;
                }
            }
            return false;
        }

    }

    private static void writeFrames(ArrayList<ArrayList<String[]>> frameLines, String outputDir, String regions) {
        File outputFile = new File(outputDir + "/" + regions.replaceAll(",","_") + ".txt");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < frameLines.size(); i++) {
            ArrayList<String[]> thisFrame = frameLines.get(i);
            if(thisFrame.size() > 1) {
                String prefix = "";
                for (int j = 0; j < thisFrame.size(); j++) {
                    sb.append(prefix).append(thisFrame.get(j)[0]).append(",").append(thisFrame.get(j)[1]);
                    prefix = ",";
                }
                sb.append("\r\n");
            }
        }

        try {
            Files.deleteIfExists(outputFile.toPath());
            Files.write(outputFile.toPath(), sb.toString().getBytes(),
                    StandardOpenOption.CREATE_NEW);
            logger.info("File written at: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
