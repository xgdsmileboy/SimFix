#!/usr/bin/python

import gzip
import sys
import argparse
import pyfl

parser = argparse.ArgumentParser()
parser.add_argument('killmap')

args = parser.parse_args()

print(' '.join(str(mutant_id) for mutant_id in pyfl.get_behavior_changing_mutants(pyfl.iter_killmap_test_runs(gzip.open(args.killmap, 'rt')))))
