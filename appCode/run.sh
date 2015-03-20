#! /bin/bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/tuples.tsv ] \
  || ! [ -f data/source_group.csv ] \
  || ! [ -f data/source_tuples.csv ]; then
  echo "ERROR: Data files do not exist. You should download the data from http://i.stanford.edu/hazy/deepdive-tutorial-data.zip, and extract files to data/ directory."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`
export DEEPDIVE_HOME=`cd $(dirname $0)/../../; pwd`

# Database Configuration
export DBNAME=manasrj_deepdive

export PGUSER=senwu
export PGPASSWORD=${PGPASSWORD:-}
export PGPORT=6432
export PGHOST=raiders2

# Initialize database
#bash $APP_HOME/setup_database.sh $DBNAME

# Using ddlib
export PYTHONPATH=$DEEPDIVE_HOME/ddlib:$PYTHONPATH

cd $DEEPDIVE_HOME

# Run DeepDive
set -e
# SBT_OPTS="-Xmx4g" sbt "run -c $APP_HOME/application.conf"
deepdive -c $APP_HOME/application.conf

# Generate automatic reports
#cd $APP_HOME
#braindump
