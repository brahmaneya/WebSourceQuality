#! /usr/bin/env bash

# Check the data files
if ! [ -d data ] \
  || ! [ -f data/source_book_author.csv ] \
  || ! [ -f data/book_author.csv ] \
  || ! [ -f data/labeled_book_author.csv ]; then
  echo "ERROR: Data files do not exist."
  exit 1;
fi

export APP_HOME=`cd $(dirname $0)/; pwd`

deepdive sql "copy source_outputs_input from STDIN CSV;" < $APP_HOME/data/srcBookAuthor.csv
deepdive sql "copy tuples_input from STDIN CSV;" < $APP_HOME/data/bookAuthor.csv
deepdive sql "copy labeled_tuples_input from STDIN CSV;" < $APP_HOME/data/bookAuthorTrue.csv
