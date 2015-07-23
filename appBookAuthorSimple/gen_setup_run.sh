#!/usr/bin/env bash 
rm -r run/
deepdive initdb
./setup_database.sh #Loads data into tables
deepdive run
deepdive sql "select * from dd_inference_result_weights_mapping order by description"
deepdive sql "select bucket, is_true, count(*) from tuple_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
