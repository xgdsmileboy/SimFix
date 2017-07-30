import os
import csv
import pyfl

import sys; sys.path.append(os.path.dirname(__file__))
import muse_optimization_comparison

os.chdir(os.environ['FL_DATA_HOME'])

with open('data/scores_artificial_vs_real_true.csv') as f:
  all_scored_faults = set((e.project, e.bug) for e in pyfl.iter_flt_score_evals(f))

n_faults = len(all_scored_faults)
safe_faults = set()
for i, (p, b) in enumerate(all_scored_faults):
  if i%100 == 0: print('{}/{}'.format(i, n_faults))
  if muse_optimization_comparison.does_any_mutant_in_any_faulty_statement_fix_any_failing_test(
      buggy_lines_path='analysis/pipeline-scripts/buggy-lines/{}-{}.buggy.lines'.format(p,b),
      mutants_log_path='killmaps/{}/{}/mutants.log'.format(p,b),
      killmap_path='killmaps/{}/{}/killmap.csv.gz'.format(p,b)):
    safe_faults.add((p, b))

unsafe_faults = all_scored_faults - safe_faults

with open('muse-optimization-comparison/unsafe-muse-faults.csv', 'w') as f:
  csv.writer(f, lineterminator='\n').writerows(unsafe_faults)
with open('muse-optimization-comparison/safe-muse-faults.csv', 'w') as f:
  csv.writer(f, lineterminator='\n').writerows(safe_faults)
