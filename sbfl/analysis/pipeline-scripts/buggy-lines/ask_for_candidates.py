#!/usr/bin/python3
#
# For each of many Defects4J bugs (provided in a CSV file),
# shows the user the patch and asks which lines are candidates
# for which faults of omission.
#
# Requirements:
#   - Python 3
#
#   - The defects4j command (of the Defects4J repository that is populated with
#     all real and artificial faults) must be on the PATH.
#
#   - The default diff viewer is meld -- you can change the diff viewer by
#     setting the --diff-viewer option.
#
# Usage:
#   python ask_for_candidates.py --output-dir <OUT DIR> <CSV file>
#
# The <CSV file> must have two (untitled) columns, specifying the
# projects/bugs of the faults to find candidates for.
#
# Example <CSV file>:
#   --------
#   bugs.csv
#   --------
#   Lang,1
#   Chart,1
#   Chart,2
#   ...
#
# Example session:
#
#     $ python ask_for_candidates.py --output-dir . bugs.csv
#
#     (prints out a bunch of stuff as it checks out all the versions)
#
#     asking about 1/3
#     Candidates for org/apache/commons/lang3/math/NumberUtils.java#467: 466,467
#     asking about 2/3
#     asking about 3/3
#     Candidates for org/jfree/data/general/DatasetUtilities.java#755: 755-757
#     Candidates for org/jfree/data/general/DatasetUtilities.java#757: 755-757
#     Candidates for org/jfree/data/general/DatasetUtilities.java#759: 757-759
#     Candidates for org/jfree/data/general/DatasetUtilities.java#761: 760-762
#     Candidates for org/jfree/data/general/DatasetUtilities.java#1242: 1242-1244
#     Candidates for org/jfree/data/general/DatasetUtilities.java#1244: 1242-1244
#     Candidates for org/jfree/data/general/DatasetUtilities.java#1246: 1245,1246
#     Candidates for org/jfree/data/general/DatasetUtilities.java#1248: 1248,1249
#     Done! Thanks a lot!
#
#     $
#

import csv
import subprocess
import collections
import argparse

PIPE = subprocess.PIPE

def check_out_dirs(project, bug, dir_format):
  buggy_dir = dir_format.format(project=project, bug=bug, bf='b')
  fixed_dir = dir_format.format(project=project, bug=bug, bf='f')
  subprocess.call(
    'defects4j checkout -p {} -v {}b -w {}'.format(
      project, bug, buggy_dir),
    shell=True)
  subprocess.call(
    'defects4j checkout -p {} -v {}f -w {}'.format(
      project, bug, fixed_dir),
    shell=True)

def remove_check_out_dirs(project, bug, dir_format):
  buggy_dir = dir_format.format(project=project, bug=bug, bf='b')
  subprocess.call('rm -rf {}'.format(buggy_dir), shell=True)
  fixed_dir = dir_format.format(project=project, bug=bug, bf='f')
  subprocess.call('rm -rf {}'.format(fixed_dir), shell=True)

def compare_dirs(project, bug, dir_format):
  buggy_dir = dir_format.format(project=project, bug=bug, bf='b')
  fixed_dir = dir_format.format(project=project, bug=bug, bf='f')
  print_source_dir_command = 'cd {} && defects4j export -p dir.src.classes'.format(buggy_dir)
  p = subprocess.Popen(print_source_dir_command, shell=True, stdout=PIPE, stderr=PIPE)
  source_dir = p.communicate()[0].decode().strip()
  subprocess.call(
    '{view} {buggy}/{source} {fixed}/{source} &'.format(
      view=args.diff_viewer,
      buggy=buggy_dir,
      fixed=fixed_dir,
      source=source_dir),
    shell=True)


def replace_line_number(line, new_number):
  return '{path}#{number}'.format(
    path=line[:line.index('#')],
    number=new_number)

if subprocess.call('which defects4j', shell=True, stdout=PIPE) != 0:
  raise RuntimeError('defects4j command not found (try adding defects4j/framework/bin to your path)')

parser = argparse.ArgumentParser()
parser.add_argument('versions_file', help='CSV file with "project,bugId" pairs to ask about')
parser.add_argument('--diff-viewer', default='meld')
parser.add_argument('--output-dir', required=True)
parser.add_argument('--skip-until', help='e.g. "Lang,8" will skip all bugs listed in the CSV until Lang 8')
parser.add_argument('--checkout-dir-format', default='/tmp/ask_for_candidates_{project}_{bug}{bf}', help="path to check projects out into, e.g. `--checkout-dir-format '/tmp/{project}_{bug}{bf}'` will check things out into /tmp/Lang_1b, /tmp/Chart_2f, etc.")

args = parser.parse_args()

with open(args.versions_file) as f:
  versions = list(csv.reader(f))

if args.skip_until is not None:
  project, bug = args.skip_until.split(',')
  versions = versions[versions.index([project, bug]):]

for i, (project, bug) in enumerate(versions, start=1):
  print('checking out {}-{} ({}/{})'.format(project, bug, i, len(versions)))
  check_out_dirs(project, bug, args.checkout_dir_format)

for i, (project, bug) in enumerate(versions, start=1):
  print('asking about {}-{} ({}/{})'.format(project, bug, i, len(versions)))
  omission_lines = [line.strip().replace('#FAULT_OF_OMISSION', '')
                    for line in open('{}-{}.buggy.lines'.format(project,bug), encoding="utf-8")
                    if 'FAULT_OF_OMISSION' in line]
  if not omission_lines:
    continue

  candidates_by_line = collections.defaultdict(set)

  compare_dirs(project, bug, args.checkout_dir_format)

  for omission_line in omission_lines:
    line_ranges_string = input('Candidates for {}: '.format(omission_line)).strip()
    line_ranges = [w.strip() for w in line_ranges_string.split(',')]

    for line_range in line_ranges:
      if '-' in line_range:
        first, last = line_range.split('-')
        first, last = int(first), int(last)
        candidates_by_line[omission_line].update(
          replace_line_number(omission_line, n)
          for n in range(first, last+1))
      else:
        candidates_by_line[omission_line].add(replace_line_number(omission_line, line_range))

  summary = ''.join(
    '{},{}\n'.format(omission_line, candidate)
    for omission_line, candidates in candidates_by_line.items()
    for candidate in sorted(candidates))

  with open('{}/{}-{}.candidates'.format(args.output_dir, project, bug), 'w') as f:
    f.write(summary)

  remove_check_out_dirs(project, bug, args.checkout_dir_format)

print('Done! Thanks a lot!')
