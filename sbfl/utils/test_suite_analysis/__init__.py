import collections
import re
import csv

Test = collections.namedtuple('Test', ['passing', 'n_stmts', 'covered_stmts'])
def iter_tests(matrix_file):
  for line in matrix_file:
    words = line.strip().split(' ')
    yield Test(
      passing=(words[-1]=='+'),
      n_stmts=len(words)-1,
      covered_stmts={i for i in range(len(words)-1) if words[i]=='1'})

def get_stmt_roots_by_line(source_code_lines_file):
  stmt_roots_by_line = {}
  for line in source_code_lines_file:
    stmt_root, spanned_line = line.split(":")
    stmt_roots_by_line[spanned_line] = stmt_root
  return stmt_roots_by_line

def get_bug_related_lines(buggy_lines_file, candidates_file):
  buggy_lines = [re.match(r'([^#]*#[0-9]*)#', line).group(1) for line in buggy_lines_file]
  candidate_lines = [candidate for buggy, candidate in csv.reader(candidates_file)]
  return buggy_lines+candidate_lines

def _classname_line_number_to_filename_line_number(s):
  classname, line_number = s.split('#')
  classname = re.sub(r'\$[^#]*', '', classname)
  classname = classname.replace('.', '/') + '.java'
  return "{}#{}".format(classname, line_number)
def get_spectra(spectra_file):
  return [_classname_line_number_to_filename_line_number(line.strip()) for line in spectra_file]

def mean(xs):
  xs = list(xs)
  return sum(xs)/len(xs)


from .get_coverage import get_coverage
from .get_redundancy import get_redundancy
from .get_intermittency import get_intermittency
