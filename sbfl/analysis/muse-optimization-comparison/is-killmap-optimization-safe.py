#!/usr/bin/python2.7

import argparse

import os; import sys; sys.path.append(os.path.dirname(__file__))
import muse_optimization_comparison

parser = argparse.ArgumentParser()
parser.add_argument('--killmap', required=True)
parser.add_argument('--mutants-log', required=True)
parser.add_argument('--buggy-lines', required=True)

args = parser.parse_args()

exit(
  0 if muse_optimization_comparison.does_any_mutant_in_any_faulty_statement_fix_any_failing_test(
    killmap_path=args.killmap,
    mutants_log_path=args.mutants_log,
    buggy_lines_path=args.buggy_lines)
  else 1)
