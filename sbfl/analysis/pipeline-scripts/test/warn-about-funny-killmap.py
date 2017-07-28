#!/usr/bin/python

import re
import itertools
import collections

def is_malformed(line):
  return not re.match(r'''
    [\w.$]+\#\w+,
    [0-9]+,
    [0-9]+,
    (PASS|FAIL|CRASH|TIMEOUT),
    (-1|[0-9]+),
    [0-9a-f]*,
    [0-9 ]*,
    .*
    ''', line, flags=re.X)

def print_warnings(lines, prefix):
  lines_grouped_by_test = itertools.groupby(lines, (lambda line: line[:line.index(',')]))

  n_times_tests_seen = collections.defaultdict(int)

  for test, lines_for_test in lines_grouped_by_test:
    n_times_tests_seen[test] += 1
    n = n_times_tests_seen[test]

    if n > 1:
      print('{prefix}: saw {test} {n} times'.format(prefix=prefix, n=n, test=test))

    first_line_for_test = next(lines_for_test)
    if not first_line_for_test.startswith(test+',0,'):
      print('{prefix}: {n}th group of lines for {test} does not start with mutant 0'.format(prefix=prefix, n=n, test=test))

    if is_malformed(first_line_for_test) or any(is_malformed(l) for l in lines_for_test):
      print('{prefix}: {n}th group of lines for {test} has malformed lines'.format(prefix=prefix, n=n, test=test))

import argparse
parser = argparse.ArgumentParser()
parser.add_argument('killmaps', nargs='+', help='killmap files to inspect for weirdness')
args = parser.parse_args()

for path in args.killmaps:
  with open(path) as f:
    print_warnings(iter(f), prefix=path)
