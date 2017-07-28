import argparse
import csv
import sys
import os

import test_suite_differences

parser = argparse.ArgumentParser()
parser.add_argument('--no-header', action="store_true")
parser.add_argument('project')
parser.add_argument('bug')
parser.add_argument('test_suite')
parser.add_argument('source_code_lines')
parser.add_argument('buggy_lines')
parser.add_argument('candidates')
parser.add_argument('spectra')
parser.add_argument('matrix')

args = parser.parse_args()

with open(args.source_code_lines) as f:
  stmt_roots = test_suite_differences.get_stmt_roots_by_line(f)

if not os.path.exists(args.candidates):
  args.candidates = '/dev/null'
with open(args.buggy_lines) as buggy_lines_file, open(args.candidates) as candidates_file:
  bug_related_lines = test_suite_differences.get_bug_related_lines(buggy_lines_file, candidates_file)

with open(args.spectra) as f:
  line_ordering = test_suite_differences.get_spectra(f)

with open(args.matrix) as f:
  tests = list(test_suite_differences.iter_tests(f))


writer = csv.DictWriter(sys.stdout, fieldnames=['Project', 'Bug', 'TestSuite', 'Coverage', 'Redundancy', 'Intermittency'])

if not args.no_header:
  writer.writeheader()

writer.writerow({
  'Project': args.project,
  'Bug': args.bug,
  'TestSuite': args.test_suite,
  'Coverage': test_suite_differences.get_coverage(tests),
  'Redundancy': test_suite_differences.get_redundancy(tests),
  'Intermittency': test_suite_differences.get_intermittency(tests, line_ordering, stmt_roots, bug_related_lines)})
