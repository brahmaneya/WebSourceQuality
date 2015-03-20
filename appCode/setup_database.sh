#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/source_group.csv ] \
  || ! [ -f data/source_tuples.csv ]; then
  echo "ERROR: Data files do not exist. You should download the data from http://i.stanford.edu/hazy/deepdive-tutorial-data.zip, and extract files to data/ directory."
  exit 1;
fi

if [ $# = 1 ]; then
  export DBNAME=$1
else
  echo "Usage: bash setup_database DBNAME"
  DBNAME=manasrj_deepdive
fi
echo "Set DB_NAME to ${DBNAME}."
echo "HOST is ${PGHOST}, PORT is ${PGPORT}."

dropdb $DBNAME
createdb $DBNAME

export APP_HOME=`cd $(dirname $0)/; pwd`

psql -d $DBNAME < $APP_HOME/schema.sql
psql -d $DBNAME -c "copy source_groups from STDIN CSV;" < $APP_HOME/data/source_group.csv
psql -d $DBNAME -c "copy source_outputs from STDIN CSV;" < $APP_HOME/data/source_tuples.csv
