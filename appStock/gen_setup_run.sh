#!/usr/bin/env bash 
rm -r run/
deepdive initdb
./setup_database.sh #Loads data into tables
deepdive run
#deepdive sql "select * from dd_inference_result_weights_mapping order by description"
deepdive sql "select bucket, is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"
