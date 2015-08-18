#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/srcFeatures.csv ] \
  || ! [ -f data/srcStockVolumes.csv ] \
  || ! [ -f data/stockVolumes.csv ]; then
  echo "ERROR: Data files do not exist."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy source_stock_input from STDIN CSV;" < $APP_HOME/data/srcStockVolumes.csv
deepdive sql "copy stock_input from STDIN CSV;"< $APP_HOME/data/stockVolumes.csv
deepdive sql "copy source_features from STDIN CSV;" < $APP_HOME/data/srcFeatures.csv
