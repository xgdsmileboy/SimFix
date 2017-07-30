'''Fault-localization-project-related Python utilities.

Contains functions to parse the data in various files, such as killmaps, buggy-line files and buggy-line-candidate files, and mutant-log files.
'''

import itertools
from .formats import *

def get_behavior_changing_mutants(killmap_test_runs):
  result = set()
  for test, runs in itertools.groupby(killmap_test_runs, (lambda run: run.test)):
    runs = iter(runs)
    first_run = next(runs)
    for run in runs:
      if (run.category != first_run.category) or (run.hash != first_run.hash) or (run.traceback != first_run.traceback):
        result.add(run.mutant_id)
  return result

def get_mutant_path_and_line(mutant):
  return '{classpath}.java#{lineno}'.format(
    classpath=mutant.classname.replace('.', '/'),
    lineno=mutant.lineno)

def iter_mutants_in_scope(mutants, stmt_spans_file, fixed_lines_file):
  with open(stmt_spans_file) as f:
    stmt_roots = {spanned: root for root, spanned in csv.reader(f, delimiter=':')}

  with open(fixed_lines_file) as f:
    fixed_lines = set(re.match(r'[^#]+#\d+', line).group() for line in f)
  fixed_stmt_roots = set(stmt_roots.get(line, line) for line in fixed_lines)

  for mutant in mutants:
    path_and_line = get_mutant_path_and_line(mutant)
    stmt_root = stmt_roots.get(path_and_line, path_and_line)
    if stmt_root in fixed_stmt_roots:
      yield mutant
