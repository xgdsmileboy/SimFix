import collections
from . import Test, mean

def get_redundancy(tests):
  n_stmts = tests[0].n_stmts
  n_covering_tests_by_stmt = collections.Counter()
  for test in tests:
    n_covering_tests_by_stmt.update(test.covered_stmts)

  return mean(n for n in n_covering_tests_by_stmt.values() if n>0)

assert 1   == get_redundancy([Test(passing=True, n_stmts=4, covered_stmts={1})])
assert 1   == get_redundancy([Test(passing=True, n_stmts=4, covered_stmts={0,1})])
assert 5/3 == get_redundancy([Test(passing=True, n_stmts=4, covered_stmts={0,1}),
                                       Test(passing=True, n_stmts=4, covered_stmts={0,1,2})])
