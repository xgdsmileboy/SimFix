#!/usr/bin/python3

'''

Usage:

  python get_coverage.py FILE

where FILE is a coverage matrix of the format produced by GZoltar.

Prints to stdout the fraction of statements that are covered by at least one test
  (i.e. the number of columns in the matrix with at least one 1)
'''

from __future__ import division
import collections

from . import Test

def get_coverage(tests):
  # import pdb; pdb.set_trace()
  n_stmts = tests[0].n_stmts
  covered_stmts = set.union(*[test.covered_stmts for test in tests])
  return len(covered_stmts) / n_stmts

assert 1/4 == get_coverage([Test(passing=True, n_stmts=4, covered_stmts={1})])
assert 1/4 == get_coverage([Test(passing=True, n_stmts=4, covered_stmts={1}),
                            Test(passing=True, n_stmts=4, covered_stmts={1})])
assert 2/4 == get_coverage([Test(passing=True, n_stmts=4, covered_stmts={1}),
                            Test(passing=True, n_stmts=4, covered_stmts={2})])
