#This is a python file to run the necessary experiments for feature based truth discovery


import logging
import commands
import timeit
import os

def initDD():
	commands.getstatusoutput('rm -r run/')
	commands.getstatusoutput('deepdive initdb')
	commands.getstatusoutput('deepdive sql "copy source_features from STDIN CSV;" < data/srcFeatures.csv')
	#commands.getstatusoutput('deepdive sql "copy source_features from STDIN CSV;" < data/srcMentionFeats.csv')
	commands.getstatusoutput('deepdive sql "copy event_input from STDIN CSV;" < data/groundtruth.csv')
	commands.getstatusoutput('deepdive sql "copy eventPosVotes from STDIN CSV;" < data/positiveVotes.csv')
	commands.getstatusoutput('deepdive sql "copy eventPosVotes from STDIN CSV;" < data/positiveVotesAcross.csv')
	commands.getstatusoutput('deepdive sql "copy eventNegVotes from STDIN CSV;" < data/negativeVotes.csv')
	
# create directory to store results
commands.getstatusoutput('mkdir expResults')

# Initialize logging
logger = logging.getLogger("exps")
logger.setLevel(logging.DEBUG)

fh = logging.FileHandler(os.path.join(os.path.dirname(os.path.realpath("__file__")), "event_experiments_gibbs_perf_nem.log"),"w")
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

holdoutSizes = ['0.99','0.98','0.95', '0.9', '0.8', '0.6', '0.4']
# Run experiments for truth disdovery without features
logger.info('EXPERIMENTS: SOURCES ONLY')
for h in holdoutSizes:
 	logger.info('USING HOLDOUT '+str(h))
 	logger.info('START experiments: SOURCES ONLY w. HOLDOUT '+str(h))
	
 	# Set holdout set
	commands.getstatusoutput('sed -i \'s/calibration.holdout_fraction: [0-9].[0-9]\+/calibration.holdout_fraction: '+h+'/g\' confsForExps/sources_only_nem.conf')
 	#commands.getstatusoutput('sed -i \'s/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT [0-9]\+)/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT '+str(int(round(h*907)))+')/g\' confsForExps/sources_only_gibbs.conf')
 	# init dd
 	logger.info('Initializing and cleaning DD')
 	initDD()
 	logger.info('DONE')
 	start = timeit.default_timer()
 	commands.getstatusoutput('deepdive run -c confsForExps/sources_only_nem.conf')
 	stop = timeit.default_timer()
 	logger.info('DONE running SOURCES ONLY. Total run time = '+str(round(stop-start,2))+" secs")
 	# Print results 
 	logger.debug('PRINT results: SOURCES ONLY')
 	# Compute source-accuracy estimation error	
 	output = commands.getstatusoutput('deepdive sql "select sqrt(sum(acc_errors.acc_error*acc_errors.acc_error)::float/count(*)::float) from (select s_est.source_id, s_est.est_acc, s_cor.acc as act_acc, abs(s_est.est_acc - s_cor.acc) as acc_error from (select s.source_id as source_id, 1/(1+exp(-1*w1.weight)) as est_acc, w1.weight as weight from (select distinct(source_id) from source_features) as s, (select description, weight from dd_inference_result_weights as t1, dd_graph_weights as t2 where t1.id = t2.id and description like \'%_equal-%\') as w1 where w1.description like \'%\'||s.source_id) as s_est, (select t1.source_id, t1.total, (case when t2.correct is null then 0 else t2.correct end) as correct, (case when t2.correct is null then 0 else t2.correct end)::float/t1.total::float as acc from (select source_id, count(*) total from source_event_truth group by source_id) as t1 left outer join (select source_id, count(*) as correct from source_event_truth as st, event_truth as et where st.geid = et.geid and st.is_true = et.is_true group by source_id) as t2 on t1.source_id = t2.source_id) as s_cor where s_est.source_id = s_cor.source_id) as acc_errors"');
 	logger.info('ACCURACY ESTIMATION ERROR:\n'+output[1])
 	# Compute buckets and assignments
 	output = commands.getstatusoutput('deepdive sql "select bucket, is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"')
 	logger.debug('BUCKETS:\n'+output[1])
 	# Compute confusion matrix for threshold 0.5
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 5 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 5 group by is_true"');
 	# Parse output and grab values
 	try:
		values = output[1]
 		values = values.split('\n')
 		confMatrixEntries = {}
 		confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.5')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:
		logger.info('Problem with confusion matrix')

	# Compute confusion matrix for threshold 0.6
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 6 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 6 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.6')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
 	# Compute confusion matrix for threshold 0.7
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 7 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 7 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.7')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
	# Compute confusion matrix for threshold 0.8
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 8 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 8 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.8')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
 	# Compute confusion matrix for threshold 0.9
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 9 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 9 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.9')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
 	# Evaluate assingments to false for threshold 0.5
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"')
 	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.5)\n'+output[1])
 	# Evaluate assingments to true for threshold 0.5
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"')
 	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.5)\n'+output[1])	
 	# Evaluate assingments to false for threshold 0.3
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 3 group by is_true order by is_true"')
 	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.3)\n'+output[1])
 	# Evaluate assingments to true for threshold 0.7
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 7 group by is_true order by is_true"')
 	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.7)\n'+output[1])
 	# Evaluate assingments to false for threshold 0.1
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 1 group by is_true order by is_true"')
 	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.1)\n'+output[1])
 	# Evaluate assingments to true for threshold 0.9
 	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 9 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.9)\n'+output[1])
 	logger.info('END experiments: SOURCES ONLY')

# Run experiments for truth disdovery without features
logger.info('EXPERIMENTS: WITH FEATURES')
for h in holdoutSizes:
	logger.info('USING HOLDOUT '+str(h))
	logger.info('START experiments: WITH FEATURES ONLY w. HOLDOUT '+str(h))

	# Set holdout set
	commands.getstatusoutput('sed -i \'s/calibration.holdout_fraction: [0-9].[0-9]\+/calibration.holdout_fraction: '+h+'/g\' confsForExps/sources_w_features_nem.conf')
	#commands.getstatusoutput('sed -i \'s/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT [0-9]\+)/calibration.holdout_query: "INSERT INTO dd_graph_variables_holdout(variable_id) SELECT id FROM stock_truth WHERE stock_symbol in (SELECT DISTINCT stock_symbol FROM stock_truth WHERE is_true ORDER BY stock_symbol LIMIT '+str(int(round(h*907)))+')/g\' confsForExps/sources_w_features_gibbs.conf')
	# init dd
	logger.info('Initializing and cleaning DD')
	initDD()
	logger.info('DONE')

	start = timeit.default_timer()
	commands.getstatusoutput('deepdive run -c confsForExps/sources_w_features_nem.conf')
	stop = timeit.default_timer()
	logger.info('DONE running WITH FEATURES. Total run time = '+str(round(stop-start,2))+" secs")
	# Print results 
	logger.debug('PRINT results: WITH FEATURES')
	# Compute source-accuracy estimation error	
	output = commands.getstatusoutput('deepdive sql "select sqrt(sum(acc_errors.acc_error*acc_errors.acc_error)::float/count(*)::float) from (select s_est.source_id, s_est.est_acc, s_cor.acc, abs(s_est.est_acc - s_cor.acc) as acc_error from (select source_id, sum(w), 1/(1+exp(-1*sum(w)::float)) as est_acc from (select t.source_id,t.feature,t2.weight as w from source_features as t, (select * from dd_inference_result_weights_mapping where description like \'%_equal%\') as t2 where t2.description like \'%-\'||t.feature order by source_id) as ag group by source_id) as s_est, (select t1.source_id, t1.total, (case when t2.correct is null then 0 else t2.correct end) as correct, (case when t2.correct is null then 0 else t2.correct end)::float/t1.total::float as acc from (select source_id, count(*) total from source_event_truth group by source_id) as t1 left outer join (select source_id, count(*) as correct from source_event_truth as st, event_truth as et where st.geid = et.geid and st.is_true = et.is_true group by source_id) as t2 on t1.source_id = t2.source_id) as s_cor where s_est.source_id = s_cor.source_id) as acc_errors"')
	logger.info('ACCURACY ESTIMATION ERROR:\n'+output[1])
	# Pring feature weights
	output = commands.getstatusoutput('deepdive sql "select distinct t1.feature, t2.weight from source_features as t1, dd_inference_result_weights_mapping as t2 where t2.description like \'%_equal-\'||t1.feature order by t2.weight desc"')
	logger.debug('SELECTED FEATURES W. WEIGHTS:\n'+output[1])
	# Compute buckets and assignments
	output = commands.getstatusoutput('deepdive sql "select bucket, is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL group by bucket, is_true order by bucket, is_true"')
	logger.debug('BUCKETS:\n'+output[1])
	# Compute confusion matrix for threshold 0.5
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 5 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 5 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.5')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')

	# Compute confusion matrix for threshold 0.6
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 6 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 6 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.6')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
 	# Compute confusion matrix for threshold 0.7
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 7 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 7 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.7')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
	# Compute confusion matrix for threshold 0.8
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 8 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 8 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.8')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
 	# Compute confusion matrix for threshold 0.9
 	output = commands.getstatusoutput('deepdive sql "select \'f\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket < 9 group by is_true union select \'t\' as assigned, is_true, count(*) as c from event_truth_is_true_inference_bucketed where is_true is not null and bucket >= 9 group by is_true"');
 	# Parse output and grab values
 	try:
	 	values = output[1]
	 	values = values.split('\n')
	 	confMatrixEntries = {}
	 	confMatrixEntries['tt'] = 0
	 	confMatrixEntries['ft'] = 0
	 	confMatrixEntries['tf'] = 0
	 	confMatrixEntries['ff'] = 0
	 	for vidx in [2,3,4,5]:
	 		cur_line = values[vidx]
	 		cur_line = cur_line.replace(' ','').split('|')
	 		confMatrixEntries[cur_line[0]+cur_line[1]] = float(cur_line[2])
	 	# Compute Precision, Recall, F1
	 	precision = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['tf'])
	 	recall = confMatrixEntries['tt']/(confMatrixEntries['tt'] + confMatrixEntries['ft'])
	 	acc = (confMatrixEntries['tt'] + confMatrixEntries['ff'])/(confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'] + confMatrixEntries['ff'])
	 	f1 = (2.0*confMatrixEntries['tt'])/(2*confMatrixEntries['tt'] + confMatrixEntries['tf'] + confMatrixEntries['ft'])
	 	logger.info('\nPERFORMANCE FOR Threshold 0.9')
	 	logger.info('Precision: '+str(precision))
	 	logger.info('Recall: '+str(recall))
	 	logger.info('Acc: '+str(acc))
	 	logger.info('F1: '+str(f1))
	except:	
		logger.info('Problem with confusion matrix')
	# Evaluate assingments to false for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.5)\n'+output[1])
	# Evaluate assingments to true for threshold 0.5
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 5 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.5)\n'+output[1])
	# Evaluate assingments to false for threshold 0.3
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 3 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.3)\n'+output[1])
	# Evaluate assingments to true for threshold 0.7
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 7 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.7)\n'+output[1])
	# Evaluate assingments to false for threshold 0.1
	output = commands.getstatusoutput('deepdive sql "select \'Assigned False\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket < 1 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO FALSE (threshold 0.1)\n'+output[1])
	# Evaluate assingments to true for threshold 0.9
	output = commands.getstatusoutput('deepdive sql "select \'Assigned True\', is_true, count(*) from event_truth_is_true_inference_bucketed where is_true IS NOT NULL and bucket >= 9 group by is_true order by is_true"')
	logger.debug('EVALUATE ASSIGNMENTS TO TRUE (threshold 0.9)\n'+output[1])
	logger.info('END experiments: WITH FEATURES')

# backup 
commands.getstatusoutput('cp event_experiments_gibbs_perf_nem.log expResults/.')
