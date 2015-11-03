#This is a python file to run the necessary experiments for feature based truth discovery


import logging
import commands
import timeit
import os

def initDD():
	commands.getstatusoutput('rm -r run/')
	commands.getstatusoutput('deepdive initdb')
	commands.getstatusoutput('deepdive sql "copy source_stock_input from STDIN CSV;" < data/srcStockVolumes.csv')
	commands.getstatusoutput('deepdive sql "copy stock_input from STDIN CSV;"< data/stockVolumes.csv')
	commands.getstatusoutput('deepdive sql "copy source_features from STDIN CSV;" < data/srcFeatures.csv')

# create directory to store results
commands.getstatusoutput('mkdir expResults')

# Initialize logging
logger = logging.getLogger("exps")
logger.setLevel(logging.DEBUG)

fh = logging.FileHandler(os.path.join(os.path.dirname(os.path.realpath("__file__")), "stock_experiments_test_gibbs.log"),"w")
fh.setLevel(logging.DEBUG)
formatter = logging.Formatter("%(name)s - %(levelname)s - %(message)s")
fh.setFormatter(formatter)
logger.addHandler(fh)


# create console handler and set level to debug
ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
formatter = logging.Formatter("%(name)s - %(levelname)s - %(message)s")
ch.setFormatter(formatter)
logger.addHandler(ch)

# Define different holdout sizes

holdoutSizes = [0.8, 0.6, 0.4, 0.2]

# Run experiments for truth disdovery without features
logger.info('EXPERIMENTS: SOURCES ONLY')
for h in holdoutSizes:
	logger.info('USING HOLDOUT '+str(h))
	logger.info('START experiments: SOURCES ONLY w. HOLDOUT '+str(h))
	
	# Set holdout set
	commands.getstatusoutput('sed -i \'s/calibration.holdout_fraction: [0-9].[0-9]/calibration.holdout_fraction: '+str(h)+'/g\' confsForExps/sources_only_test.conf')
	#commands.getstatusoutput('sed -i \'s/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT [0-9]\+)/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT '+str(int(round(h*907)))+')/g\' confsForExps/sources_only.conf')
	# init dd
	logger.info('Initializing and cleaning DD')
	initDD()
	logger.info('DONE')
	start = timeit.default_timer()
	commands.getstatusoutput('deepdive run -c confsForExps/sources_only_test.conf')
	stop = timeit.default_timer()
	logger.info('DONE running SOURCES ONLY. Total run time = '+str(round(stop-start,2))+" secs")
	# Print results 
	logger.debug('PRINT results: SOURCES ONLY')
	# Compute source-accuracy estimation error
	output = commands.getstatusoutput('deepdive sql "select sqrt(sum(acc_errors.acc_error*acc_errors.acc_error)::float/count(*)::float) from (select s_est.source_id, s_est.est_acc, s_cor.acc as act_acc, abs(s_est.est_acc - s_cor.acc) as acc_error from (select s.source_id as source_id, 1/(1+exp(-0.5*(w1.weight-w2.weight))) as est_acc, w1.weight as a_weight, w2.weight as d_weight from (select distinct(source_id) from source_features) as s, (select description, weight from dd_inference_result_weights as t1, dd_graph_weights as t2 where t1.id = t2.id and description like \'%_true-%\') as w1, (select description, weight from dd_inference_result_weights as t1, dd_graph_weights as t2 where t1.id = t2.id and description like \'%_false-%\') as w2 where w1.description like \'%\'||s.source_id and w2.description like \'%\'||s.source_id) as s_est, (select src_total.source_id, correct, total, correct::float/total::float as acc from (select source_id, count(*) as total from source_stock_truth where is_true group by source_id) as src_total left outer join (select source_id, count(*) as correct from (select t1.source_id, t2.stock_tuple_id from source_stock_truth as t1, stock_truth as t2 where t1.is_true and t2.is_true and t1.stock_tuple_id = t2.stock_tuple_id) as cor group by source_id) as src_cor_total on src_total.source_id = src_cor_total.source_id) as s_cor where s_est.source_id = s_cor.source_id) as acc_errors"')
	logger.info('ACCURACY ESTIMATION ERROR:\n'+output[1])
	# Compute buckets and assignments
	output = commands.getstatusoutput('deepdive sql "select bucket, is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"')
	logger.debug('BUCKETS:\n'+output[1])
	# Evaluate assingments to false for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.5)\n'+output[1])
	# Evaluate assingments to true for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.5)\n'+output[1])	
	# Evaluate assingments to false for threshold 0.3
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 3 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.3)\n'+output[1])
	# Evaluate assingments to true for threshold 0.7
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 7 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.7)\n'+output[1])
	# Evaluate assingments to false for threshold 0.1
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 1 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.1)\n'+output[1])
	# Evaluate assingments to true for threshold 0.9
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 9 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.9)\n'+output[1])
	logger.info('END experiments: SOURCES ONLY')


# Run experiments for truth disdovery without features
logger.info('EXPERIMENTS: WITH FEATURES')
for h in holdoutSizes:
	logger.info('USING HOLDOUT '+str(h))
	logger.info('START experiments: WITH FEATURES ONLY w. HOLDOUT '+str(h))

	# Set holdout set
	commands.getstatusoutput('sed -i \'s/calibration.holdout_fraction: [0-9].[0-9]/calibration.holdout_fraction: '+str(h)+'/g\' confsForExps/sources_w_features_test.conf')
	#commands.getstatusoutput('sed -i \'s/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT [0-9]\+)/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT '+str(int(round(h*907)))+')/g\' confsForExps/sources_w_features.conf')
	# init dd
	logger.info('Initializing and cleaning DD')
	initDD()
	logger.info('DONE')

	start = timeit.default_timer()
	commands.getstatusoutput('deepdive run -c confsForExps/sources_w_features_test.conf')
	stop = timeit.default_timer()
	logger.info('DONE running WITH FEATURES. Total run time = '+str(round(stop-start,2))+" secs")
	# Print results 
	logger.debug('PRINT results: WITH FEATURES')
	# Compute source-accuracy estimation error	
	output = commands.getstatusoutput('deepdive sql "select sqrt(sum(acc_errors.acc_error*acc_errors.acc_error)::float/count(*)::float) from (select s_est.source_id, s_est.est_acc, s_cor.acc as act_acc, abs(s_est.est_acc - s_cor.acc) as acc_error from ( select source_id, sum(w), 1/(1+exp(sum(w)::float)) as est_acc from ( select ag.source_id, ag.feature, -0.5*(ag.weight - dis.weight) as w from (select t.source_id,t.feature,t2.weight from source_features as t, (select * from dd_inference_result_weights_mapping where description like \'%_true%\') as t2 where t2.description like \'%-\'||t.feature order by source_id) as ag, (select t.source_id,t.feature,t2.weight from source_features as t, (select * from dd_inference_result_weights_mapping where description like \'%_false%\') as t2 where t2.description like \'%-\'||t.feature order by source_id) as dis where ag.source_id = dis.source_id and ag.feature = dis.feature) src_w group by source_id) as s_est, (select src_total.source_id, correct, total, correct::float/total::float as acc from (select source_id, count(*) as total from source_stock_truth where is_true group by source_id) as src_total left outer join (select source_id, count(*) as correct from (select t1.source_id, t2.stock_tuple_id from source_stock_truth as t1, stock_truth as t2 where t1.is_true and t2.is_true and t1.stock_tuple_id = t2.stock_tuple_id) as cor group by source_id) as src_cor_total on src_total.source_id = src_cor_total.source_id) as s_cor where s_est.source_id = s_cor.source_id) as acc_errors"')
	logger.info('ACCURACY ESTIMATION ERROR:\n'+output[1])
	# Pring feature weights
	output = commands.getstatusoutput('deepdive sql "select distinct t.feature, t1.weight as true_w, t2.weight as false_w, 0.5*(t1.weight -t2.weight) as diff from source_features as t, dd_inference_result_weights_mapping as t1, dd_inference_result_weights_mapping as t2 where t1.description like \'%_true-\'||t.feature and t2.description like \'%_false-\'||t.feature order by diff desc"')
	logger.debug('SELECTED FEATURES W. WEIGHTS:\n'+output[1])
	# Compute buckets and assignments
	output = commands.getstatusoutput('deepdive sql "select bucket, is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"')
	logger.debug('BUCKETS:\n'+output[1])
	# Evaluate assingments to false for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.5)\n'+output[1])
	# Evaluate assingments to true for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.5)\n'+output[1])
	# Evaluate assingments to false for threshold 0.3
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 3 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.3)\n'+output[1])
	# Evaluate assingments to true for threshold 0.7
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 7 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.7)\n'+output[1])
	# Evaluate assingments to false for threshold 0.1
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 1 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.1)\n'+output[1])
	# Evaluate assingments to true for threshold 0.9
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from stock_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 9 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.9)\n'+output[1])
	logger.info('END experiments: WITH FEATURES')

# backup 
commands.getstatusoutput('cp stock_experiments_test_gibbs.log expResults/.')
