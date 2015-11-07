#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/srcFeatures.csv ] \
  || ! [ -f data/srcMentionFeats.csv ] \
  || ! [ -f data/positiveVotes.csv] \
  || ! [ -f data/negativeVotes.csv ] \
  || ! [ -f data/groundtruth.csv ]; then
  echo "ERROR: Data files do not exist."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy source_features from STDIN CSV;" < $APP_HOME/data/srcFeatures.csv
deepdive sql "copy source_features from STDIN CSV;" < $APP_HOME/data/srcMentionFeats.csv
deepdive sql "copy event_input from STDIN CSV;" < $APP_HOME/data/groundtruth.csv
deepdive sql "copy eventPosVotes STDIN CSV;" < $APP_HOME/data/positiveVotes.csv
deepdive sql "copy eventNegVotes STDIN CSV;" < $APP_HOME/data/negativeVotes.csv
