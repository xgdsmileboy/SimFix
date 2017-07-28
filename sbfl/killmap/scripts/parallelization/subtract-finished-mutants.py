#!/usr/bin/python

import sys
import argparse
import itertools
import collections
import csv
import gzip
import pyfl

parser = argparse.ArgumentParser()
parser.add_argument('killmap')
args = parser.parse_args()

finished_mutant_ids = collections.defaultdict(set)
for run in pyfl.iter_killmap_test_runs(gzip.open(args.killmap, 'rt')):
  finished_mutant_ids[run.test].add(run.mutant_id)

writer = csv.DictWriter(sys.stdout, fieldnames=['Test', 'Mutants'])
writer.writeheader()
for row in csv.DictReader(sys.stdin):
  test = row['Test']
  mutant_ids = {int(m) for m in row['Mutants'].split(' ') if m}
  unfinished_mutant_ids = mutant_ids - finished_mutant_ids[test]
  if unfinished_mutant_ids:
    writer.writerow({'Test': test, 'Mutants': ' '.join(sorted(str(m) for m in unfinished_mutant_ids))})
