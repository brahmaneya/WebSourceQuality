touch '/afs/cs.stanford.edu/u/manasrj/git/WebSourceQuality/appCode/run/20150722/155251.557163533/tmp/tuple_truth.copy_query_func_ext_tuple_truth.tsv-'; find /afs/cs.stanford.edu/u/manasrj/git/WebSourceQuality/appCode/run/20150722/155251.557163533/tmp -name 'tuple_truth.copy_query_func_ext_tuple_truth.tsv-*' 2>/dev/null -print0 | xargs -0 -P 1 -L 1 bash -c '/afs/cs.stanford.edu/u/manasrj/git/WebSourceQuality/appCode/udf/ext_tuples.py < "$0" > "$0.out"'
