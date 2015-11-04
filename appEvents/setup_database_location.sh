#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/acledExtracted.csv ] \
  || ! [ -f data/africaCountryCodes.csv ] \
  || ! [ -f data/gdeltExtracted.csv ]; then
  echo "ERROR: Data files do not exist."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy gdelt_raw_input(eventid, sqldate,actor1name,actor2name,eventcode,eventbasecode,eventrootcode,actiongeo_countrycode,actiongeo_lat, actiongeo_long, sourceurl, sourcedomain) from STDIN CSV;" < $APP_HOME/data/gdeltExtracted.csv
deepdive sql "copy acled_raw_input(eventid,eventdate, event_type, country, latitude, longitude) from STDIN CSV;" < $APP_HOME/data/acledExtracted.csv
deepdive sql "copy african_countries from STDIN CSV;" < $APP_HOME/data/africaCountryCodes.csv
