#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/acledExtracted.csv ] \
  || ! [ -f data/gdeltExtracted.csv ]; then
  echo "ERROR: Data files do not exist."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy gdelt_raw_input from STDIN CSV;" < $APP_HOME/data/gdeltExtracted.csv
deepdive sql "copy aclead_raw_input from STDIN CSV;" < $APP_HOME/data/acledExtracted.csv
