#!/usr/bin/env bash 
rm -r ~/deepdive/out/
python gen_data.py
./setup_database.sh
./run.sh
