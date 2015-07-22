#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/source_group.csv ] \
  || ! [ -f data/source_tuples.csv ]; then
  echo "ERROR: Data files do not exist. You should download the data from http://i.stanford.edu/hazy/deepdive-tutorial-data.zip, and extract files to data/ directory."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy source_groups from STDIN CSV;" < $APP_HOME/data/source_group.csv
deepdive sql "copy source_outputs_input from STDIN CSV;" < $APP_HOME/data/source_tuples.csv
deepdive sql "copy group_tuple_belief_input from STDIN CSV;" < $APP_HOME/data/group_beliefs.csv
