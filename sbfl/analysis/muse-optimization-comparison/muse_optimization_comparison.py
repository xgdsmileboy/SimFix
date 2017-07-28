import gzip
import os
import csv
import re
from pyfl import formats

def does_any_mutant_in_any_faulty_statement_fix_any_failing_test(buggy_lines_path, mutants_log_path, killmap_path):
  with open(buggy_lines_path, 'rb') as f:
    buggy_lines = formats.get_buggy_lines(f)

  with open(mutants_log_path) as f:
    ids_of_mutants_in_buggy_lines = set(m.id for m in formats.iter_mutants_log_lines(f) if formats.Line(formats.java_classname_to_path(m.classname), m.lineno) in buggy_lines)

  if not os.path.exists(killmap_path):
    raise FileNotFoundError(killmap_path)

  with gzip.open(killmap_path, mode='rt') as f:
    runs = formats.iter_killmap_test_runs(f)

    try:
      current_unmutated_run = next(runs)
      if current_unmutated_run.category != 'FAIL':
        return False
      for run in runs:
        if run.test == current_unmutated_run.test:
          if run.category == 'PASS' and run.mutant_id in ids_of_mutants_in_buggy_lines:
            return True
        elif run.category == 'FAIL':
          current_unmutated_run = run
        else:
          return False
    except (StopIteration, csv.Error):
      return False

  return False
