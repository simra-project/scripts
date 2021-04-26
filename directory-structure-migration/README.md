# Directory Structure Migration

**MOVES** files from old directory structure to new directory structure.

### Old
```
Regions
    Berlin
        Rides
        Profiles
    Ruhrgebiet
        Rides
        Profiles
```

### New
```
Regions
    Berlin
        Rides
            2020
                10
                11
                12
            2021
                01
        Profiles
    Ruhrgebiet
        Rides
            2020
                11
                12
            2021
                01
        Profiles
```

## Instructions

The latest jar is build with Java 11 to `./directory-structure-migratoin-1.0-fat.jar`.
Rebuild with `./gradlew fatJar`, copy from `./build/libs`.

```
usage: [-h] -r REGIONDIR

required arguments:
  -r REGIONDIR,           path to directory in which the regions are located
  --regionDir REGIONDIR


optional arguments:
  -h, --help              show this help message and exit
```

**MAKE A BACKUP BEFORE RUNNING THIS SCRIPT, SINCE FILES ARE MOVED!**
For example: `java -jar directory-structure-migration-1.0-fat.jar -r /sdb/SimRa/Regions`