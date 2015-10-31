#!/usr/bin/env bash 
rm -r run/
deepdive initdb
./setup_database.sh #Loads data into tables
deepdive run -c confsForExps/sources_only.conf
#deepdive sql "DROP TABLE feature_counts"
#deepdive sql "CREATE TABLE feature_counts (feature text, count int) DISTRIBUTED BY (feature);"
#deepdive sql "INSERT INTO feature_counts SELECT feature, COUNT(*) FROM source_features GROUP BY feature"
#deepdive sql "DROP TABLE feature_pair_sources"
#deepdive sql "CREATE TABLE feature_pair_sources (feature1 text, feature2 text, source_id varchar(100)) DISTRIBUTED BY (feature1, feature2);"
#deepdive sql "INSERT INTO feature_pair_sources SELECT sf1.feature, sf2.feature, sf1.source_id FROM source_features sf1, source_features sf2 WHERE sf1.source_id = sf2.source_id"
#deepdive sql "DROP TABLE feature_pair_counts"
#deepdive sql "CREATE TABLE feature_pair_counts (feature1 text, feature2 text, count1 int, count2 int, count12 int) DISTRIBUTED BY (feature1, feature2);"
#deepdive sql "INSERT INTO feature_pair_counts SELECT fps.feature1, fps.feature2, fc1.count, fc2.count, COUNT(*) FROM feature_pair_sources fps, feature_counts fc1, feature_counts fc2 WHERE fps.feature1 = fc1.feature AND fps.feature2 = fc2.feature GROUP BY fps.feature1, fps.feature2, fc1.count, fc2.count"
#deepdive sql "DROP TABLE feature_feature_correlations"
#deepdive sql "CREATE TABLE feature_feature_correlations (feature1 text, feature2 text, correlation real) DISTRIBUTED BY (feature1, feature2);"
#deepdive sql "INSERT INTO feature_feature_correlations SELECT feature1, feature2, (count12*42 - count1*count2)/(1.0*SQRT(count1*(42-count1)*count2*(42-count2))) AS correlation FROM feature_pair_counts"
#deepdive sql "select * from dd_inference_result_weights_mapping order by description"
deepdive sql "select bucket, is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"
deepdive sql "select 'True', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"
echo 'Max based evaluation for positive class'
deepdive sql "select 'True', tt.is_true, count(*) from stock_truth_is_true_inference_bucketed tt, (select stock_symbol, max(expectation) as mex from stock_truth_is_true_inference_bucketed where is_true is not null group by stock_symbol) as groupedtt where tt.stock_symbol = groupedtt.stock_symbol and tt.expectation = groupedtt.mex and tt.expectation > 0 group by tt.is_true"
echo 'Investigate weights'
deepdive sql "select distinct t.feature, fc.count, t1.weight as true_w, t2.weight as false_w, t1.weight -t2.weight as diff from source_features as t, dd_inference_result_weights_mapping as t1, dd_inference_result_weights_mapping as t2, feature_counts as fc where t.feature = fc.feature and t1.description like '%_true-'||t.feature and t2.description like '%_false-'||t.feature order by diff desc"
