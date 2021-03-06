#!/usr/bin/env bash 
rm -r ~/deepdive/out/
python gen_data.py
deepdive initdb
./setup_database.sh #Loads data into tables
deepdive run
deepdive sql "select * from dd_inference_result_weights_mapping order by description"
deepdive sql "select bucket, is_true, count(*) from tuple_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
deepdive sql "select bucket, is_true, count(*) from group_tuple_belief_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
#$PSQL "select bucket, is_true, count(*) from source_group_tuple_belief_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
