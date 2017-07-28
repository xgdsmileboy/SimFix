#!/usr/bin/python3

'''

Usage:

  python extract_coverage_info_from_matrices.py SOURCE_CODE_LINES BUGGY_LINES CANDIDATES SPECTRA MATRIX

Prints to stdout the average "intermittency" of faulty lines, i.e. the average over
all faulty lines (listed in BUGGY_LINES or CANDIDATES) of
  (# passing covering tests) / (# covering tests)
'''

from __future__ import division
import collections

def mean(xs):
  xs = list(xs)
  return sum(xs)/len(xs)

def get_faulty_statement_indices(line_ordering, stmt_roots_by_line, bug_related_lines):
  buggy_stmts = [stmt_roots_by_line.get(line, line) for line in bug_related_lines]
  return {i for i, line in enumerate(line_ordering) if line in buggy_stmts}

def get_intermittency(tests, line_ordering, stmt_roots_by_line, bug_related_lines):
  faulty_statement_indices = get_faulty_statement_indices(line_ordering, stmt_roots_by_line, bug_related_lines)
  n_covering = collections.defaultdict(int)
  n_passing_covering = collections.defaultdict(int)
  for test in tests:
    for i in test.covered_stmts:
      n_covering[i] += 1
    if test.passing:
      for i in test.covered_stmts:
        n_passing_covering[i] += 1
  return mean(n_passing_covering[i]/n_covering[i] for i in faulty_statement_indices if n_covering[i]>0)

if __name__ == '__main__':
  import unittest

  class IntermittencyTest(unittest.TestCase):
    def test_average_intermittency(self):
      self.assertEqual(
        1,
        get_average_intermittency([Test(True, {1})], {1}))
      self.assertEqual(
        0,
        get_average_intermittency([Test(False, {1})], {1}))
      self.assertEqual(
        1/3,
        get_average_intermittency([Test(True, {1}), Test(False, {1}), Test(False, {1})], {1}))

  unittest.main()


  # import argparse

  # parser = argparse.ArgumentParser()
  # parser.add_argument('matrices', nargs='+')

  # args = parser.parse_args()

  # writer = csv.DictWriter(sys.stdout, fieldnames=['CoverageFraction', 'Redundancy'])
  # writer.writeheader()
  # for matrix_file in args.matrices:
  #   with open(matrix_file) as f:
  #     matrix = parse_coverage_matrix(f.read())
  #   writer.writerow({
  #     'CoverageFraction': get_coverage_fraction(matrix),
  #     'Redundancy': get_coverage_redundancy(matrix)})
