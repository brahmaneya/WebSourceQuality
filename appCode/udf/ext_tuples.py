#! /usr/bin/env python

# Sample input data (piped into STDIN):
'''
118238@10	Sen.~^~Barack~^~Obama~^~and~^~his~^~wife~^~,~^~Michelle~^~Obama~^~,~^~have~^~released~^~eight~^~years~^~of~^~joint~^~returns~^~.	O~^~PERSON~^~PERSON~^~O~^~O~^~O~^~O~^~PERSON~^~PERSON~^~O~^~O~^~O~^~DURATION~^~DURATION~^~O~^~O~^~O~^~O
118238@12	During~^~the~^~2004~^~presidential~^~campaign~^~,~^~we~^~urged~^~Teresa~^~Heinz~^~Kerry~^~,~^~the~^~wealthy~^~wife~^~of~^~Sen.~^~John~^~Kerry~^~,~^~to~^~release~^~her~^~tax~^~returns~^~.	O~^~O~^~DATE~^~O~^~O~^~O~^~O~^~O~^~PERSON~^~PERSON~^~PERSON~^~O~^~O~^~O~^~O~^~O~^~O~^~PERSON~^~PERSON~^~O~^~O~^~O~^~O~^~O~^~O~^~O
'''

import csv, os, sys

tuples = set()
nontuples = set()
BASE_DIR = os.path.dirname(os.path.realpath(__file__))
lines = open(BASE_DIR + '/../data/tuples.tsv').readlines()
for line in lines:
  tuple_id, value = line.strip().split('\t')
  if value == 'true':
    tuples.add(tuple_id)
  else:
    nontuples.add(tuple_id)

outputted_tuples = set()
for row in sys.stdin:
  tuple_id = row.strip().split('\t')
  tuple_id = tuple_id[0].strip()
  value = '\N'
  if tuple_id in tuples:
    value = '1' 
  elif tuple_id in nontuples:
    value = '0'

  # Output a tuple for each PERSON phrase
  if tuple_id not in outputted_tuples:
    print '\t'.join([
      tuple_id, 
      value,
      '\N'   # leave "id" blank for system!
      ])
    outputted_tuples.add(tuple_id)
