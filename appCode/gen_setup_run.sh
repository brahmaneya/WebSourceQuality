#!/usr/bin/env bash 
rm -r ~/deepdive/out/
python gen_data.py
./setup_database.sh
./run.sh
$PSQL "select * from dd_inference_result_weights_mapping order by description"
