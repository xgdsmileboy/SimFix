import collections
import os
import re

Line = collections.namedtuple('Line', ['path', 'line_number'])
def iter_emptied_lines(buggy_lines_file):
  for line in buggy_lines_file:
    path, line_number, source_line = line.split(b'#', 2)
    path = path.decode()
    if source_line.strip() == b'':
      yield Line(path=path, line_number=int(line_number))

def is_interesting(line):
  return not (re.match(rb'^\s*$', line) or re.match(rb'^\s*//', line))
def iter_lines_until_interesting(source_lines, start_line, going_forward):
  in_comment = False
  start_at = min(start_line.line_number, len(source_lines))
  end_at = (len(source_lines)+1) if going_forward else 0
  line_numbers = range(start_at, end_at, 1 if going_forward else -1)
  for line_number in line_numbers:

    yield Line(path=start_line.path, line_number=line_number)

    source_line = source_lines[line_number-1]
    if re.match(rb'^\s*/[*]', source_line):
      in_comment = (True if going_forward else False)
    elif re.match(rb'^.*[*]/\s*$', source_line):
      in_comment = (False if going_forward else True)
    elif (not in_comment) and is_interesting(source_line):
      break

DO_SELFTEST = True
if DO_SELFTEST:
  def test(source_lines, start_line_number, going_forward, expected_line_numbers):
    expected = [Line('', i) for i in expected_line_numbers]
    actual = list(iter_lines_until_interesting([s.encode() for s in source_lines], Line('', start_line_number), going_forward))
    assert expected == actual, 'expected {}, got {}'.format(expected, actual)

  test(['1', '2', '3', '4'], 3, True, [3])
  test(['1', '2', '3'], 3, True, [3])
  test(['1', '2', ' ', '4'], 3, True, [3, 4])
  test(['1', '2', '  // some comment', '4'], 3, True, [3, 4])
  test(['1', '2', '3', '  // some comment', '   ', '6'], 4, True, [4,5,6])
  test(['1', '2', '3', '  /* it is a truth', ' blah blah blah', 'of uniting them */ ', '  ', '8', '9'], 4, True, [4,5,6,7,8])

  test(['1', '2', '3', '4'], 3, False, [3])
  test(['1', '2', '3'], 1, False, [1])
  test(['1', '  ', '3', '4'], 2, False, [2,1])
  test(['1', '  // some comment', '3', '4'], 2, False, [2,1])
  test(['1', '   ', ' // some comment', '4'], 3, False, [3,2,1])
  test(['1', '2', '3', '  /* it is a truth', ' blah blah blah', 'of uniting them */ ', '  ', '8', '9'], 7, False, [7,6,5,4,3])


import argparse
import subprocess
import csv

parser = argparse.ArgumentParser()
parser.add_argument('mutants_in_scope')
parser.add_argument('projects_root', help='directory containing one directory per project, each containing one directory per bug, so that e.g. Chart 10b is checked out into Chart/10')

args = parser.parse_args()


with open(args.mutants_in_scope) as f:
  for project, real_bug, artificial_bug in csv.reader(f):
    with open('{project}-{artificial_bug}.buggy.lines'.format(project=project, artificial_bug=artificial_bug), 'rb') as f:
      emptied_lines = list(iter_emptied_lines(f))

    if not emptied_lines:
      continue

    project_dir = os.path.join(args.projects_root, project, str(artificial_bug))
    subprocess.call('defects4j checkout -p {project} -v {artificial_bug}b -w {project_dir}'.format(project=project, artificial_bug=artificial_bug, project_dir=project_dir), shell=True)

    source_code_subdir_file = os.path.join(project_dir, 'srcdir.txt')
    if not os.path.exists(source_code_subdir_file):
      subprocess.call('cd {project} && defects4j export -p dir.src.classes -o {dest}'.format(project=project_dir, dest=source_code_subdir_file), shell=True)
    with open(source_code_subdir_file) as f:
      source_dir = os.path.join(project_dir, f.read().strip())

    omission_candidate_groups = collections.defaultdict(set)
    for line in emptied_lines:
      with open(os.path.join(source_dir, line.path), 'rb') as f:
        source_lines = list(f)

      for candidate in iter_lines_until_interesting(source_lines, line, going_forward=True):
        omission_candidate_groups[line].add(candidate)

      for candidate in iter_lines_until_interesting(source_lines, line._replace(line_number=line.line_number-1), going_forward=False):
        omission_candidate_groups[line].add(candidate)

      print('Lines added on account of {}:'.format(line))
      for candidate in sorted(omission_candidate_groups[line], key=(lambda line: line.line_number)):
        print('  {line_number}  {line}'.format(line_number=candidate.line_number, line=source_lines[candidate.line_number-1].rstrip().decode('ascii', errors='replace')))

    if omission_candidate_groups:
      with open('{project}-{artificial_bug}.candidates'.format(project=project, artificial_bug=artificial_bug), 'w') as f:
        for line_after_omission, candidates in sorted(omission_candidate_groups.items()):
          for candidate in sorted(candidates, key=(lambda line: line.line_number)):
            print('{0.path}#{0.line_number},{1.path}#{1.line_number}'.format(line_after_omission, candidate), file=f)
