#!/usr/bin/env bash 
rm -r run/
deepdive initdb
./setup_database.sh #Loads data into tables
deepdive run
#deepdive sql "select * from dd_inference_result_weights_mapping order by description"
deepdive sql "select bucket, is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"
echo 'Max based evaluation for positive class'
deepdive sql "select 'True', tt.is_true, count(*) from stock_truth_is_true_inference_bucketed tt, (select stock_symbol, max(expectation) as mex from stock_truth_is_true_inference_bucketed where is_true is not null group by stock_symbol) as groupedtt where tt.stock_symbol = groupedtt.stock_symbol and tt.expectation = groupedtt.mex and tt.expectation > 0 group by tt.is_true"
echo 'Max based evaluation for negative class'
deepdive sql "select 'True', tt.is_true, count(*) from stock_truth_is_true_inference_bucketed tt, (select stock_symbol, max(expectation) as mex from stock_truth_is_true_inference_bucketed where is_true is not null group by stock_symbol) as groupedtt where tt.stock_symbol = groupedtt.stock_symbol and tt.expectation = groupedtt.mex and tt.expectation = 0 group by tt.is_true"
