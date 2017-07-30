#!/usr/bin/python3

import os
import collections
import pyfl
import sys; sys.path.append(os.path.dirname(__file__))
import muse_optimization_comparison

os.chdir(os.environ['FL_DATA_HOME'])
with open('data/scores_artificial_vs_real_true.csv') as f:
  muse = [e for e in pyfl.iter_flt_score_evals(f) if e.formula == 'muse' and e.bug < 999 and e.scoring_scheme=='first']

d = collections.Counter(
  (e.score_wrt_loaded_classes < 0.05,
   muse_optimization_comparison.does_any_mutant_in_any_faulty_statement_fix_any_failing_test(e.project, e.bug))
  for e in muse)
print('''
                    Any failure fixed by any mutant in any faulty statement?
                     Y      N
MUSE does well?  Y  {:< 5d}  {:< 5d}   {}
                 N  {:< 5d}  {:< 5d}   {}
                    {:< 5d}  {:< 5d}   {}
'''.format(
  d[True, True], d[True, False], d[True,True]+d[True,False],
  d[False, True], d[False, False], d[False,True]+d[False,False],
  d[True,True]+d[False,True], d[True,False]+d[False,False], sum(d.values())))
