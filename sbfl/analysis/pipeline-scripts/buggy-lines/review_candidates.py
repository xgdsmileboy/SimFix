#!/usr/bin/python3
#
# For each of many Defects4J bugs (provided in a CSV file),
# shows the user the patch and asks whether he agrees with
# the candidates lines for each fault of omission. It writes
# to a file called 'review' the review of a reviewer, i.e.,
# whether he agrees of disagrees (and why it disagrees).
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
#   python review_candidates.py <CSV file>
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
#     $ python review_candidates.py bugs.csv
#
#     (prints out a bunch of stuff as it checks out all the versions)
#
#     asking about Time-900079 (1/2)
#     candidates:
#     org/joda/time/DateTimeZone.java#270 => ['269', '270', '271', '272']
#     Do you agree with the list of candidates? [y/n] n
#     Why not? I think line 271 should not be included as it's a '{'.
#     asking about Math-900023 (2/2)
#     candidates:
#     org/apache/commons/math3/geometry/euclidean/threed/Line.java#88 => ['87', '89']
#     Do you agree with the list of candidates? [y/n] y
#     Done! Thanks a lot!
#
#     $
#

import csv
import subprocess
import collections
import argparse
import os
import sys
import getpass

PWD = os.path.abspath(os.path.dirname(sys.argv[0]))
OUTPUT_FILE = '{}/review'.format(PWD)
USERNAME = getpass.getuser()
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


if subprocess.call('which defects4j', shell=True, stdout=PIPE) != 0:
  raise RuntimeError('defects4j command not found (try adding defects4j/framework/bin to your path)')

parser = argparse.ArgumentParser()
parser.add_argument('versions_file', help='CSV file with "project,bugId" pairs to ask about')
parser.add_argument('--diff-viewer', default='meld')
parser.add_argument('--skip-until', help='e.g. "Lang,8" will skip all bugs listed in the CSV until Lang 8')
parser.add_argument('--checkout-dir-format', default='/tmp/review_candidates_{project}_{bug}{bf}', help="path to check projects out into, e.g. `--checkout-dir-format '/tmp/{project}_{bug}{bf}'` will check things out into /tmp/Lang_1b, /tmp/Chart_2f, etc.")

args = parser.parse_args()

## Get list of bugs to analyse

with open(args.versions_file) as f:
  versions = list(csv.reader(f))

if args.skip_until is not None:
  project, bug = args.skip_until.split(',')
  versions = versions[versions.index([project, bug]):]

## File to write the review(s)

if not os.path.isfile(OUTPUT_FILE):
  output_file = open(OUTPUT_FILE, "w")
  output_file.write("project_name,bug_id,reviewer,agree(y/n),reason\n")
else:
  output_file = open(OUTPUT_FILE, "a")

## Checkout all bugs

for i, (project, bug) in enumerate(versions, start=1):
  print('checking out {}-{} ({}/{})'.format(project, bug, i, len(versions)))
  check_out_dirs(project, bug, args.checkout_dir_format)

## Review all bugs

for i, (project, bug) in enumerate(versions, start=1):
  print('asking about {}-{} ({}/{})'.format(project, bug, i, len(versions)))

  omission_lines = [line.strip().replace('#FAULT_OF_OMISSION', '')
                    for line in open('{}-{}.buggy.lines'.format(project,bug), encoding="utf-8")
                    if 'FAULT_OF_OMISSION' in line]
  if not omission_lines:
    continue

  # get candidates
  candidates_by_line = {}
  with open('{}/{}-{}.candidates'.format(PWD, project, bug), 'r') as f:
    for line in f:
      line = line.rstrip()
      buggy_line = line.split(',')[0]
      candidate_line = line.split(',')[1]
      candidate_line_number = candidate_line[candidate_line.index('#')+1:]

      if not buggy_line in candidates_by_line:
        candidates_by_line[buggy_line] = []
      candidates_by_line[buggy_line].append(candidate_line_number)  

  print('candidates:')
  for key in candidates_by_line:
    print("{} => {}".format(key, candidates_by_line[key]))

  # open diff
  compare_dirs(project, bug, args.checkout_dir_format)

  # review
  review = '{},{},{}'.format(project, bug, USERNAME)
  while True:
    choice = input('Do you agree with the list of candidates? [y/n] ').strip().lower()
    if choice == 'y':
      output_file.write('{},{},{}\n'.format(review, 'y', ''))
      break
    elif choice == 'n':
      while True:
        reason = input('Why not? ').strip()
        output_file.write('{},{},{}\n'.format(review, 'n', reason))
        break
      break
    else:
      print("Please respond with 'y' for 'yes' or 'n' for 'no'!")

  remove_check_out_dirs(project, bug, args.checkout_dir_format)

print('Done! Thanks a lot!')
