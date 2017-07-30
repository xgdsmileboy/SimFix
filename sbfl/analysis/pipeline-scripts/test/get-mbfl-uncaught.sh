#!/bin/bash
#
# Prints a list of faulty lines that will be unranked by MBFLTs.
# The output has the same structure as the pipeline's .buggy.lines files.
#
# Usage:
#   get-mbfl-uncaught.sh PROJECT BUG MUTANTS_LOG DIR
# e.g.
#   get-mbfl-uncaught.sh Chart 1 Chart/1/mutants.log /tmp/Chart-1
#

P=$1
B=$2
MUTANTS_LOG=$3
DIR=$4

die() { echo "$@" >&2; exit 1; }
USAGE="$0 PROJECT BUG MUTANTS_LOG DIR"
if [ ! "$ANALYSIS_HOME" ]; then die "ANALYSIS_HOME must be set"; fi
if [ "$#" != 4 ]; then die "usage: $USAGE"; fi

mkdir -p "$DIR"

# generate a fake mutant-kill matrix to run the pipeline with
python -c "
n=len(open('$MUTANTS_LOG').readlines())
open('$DIR/dummy.matrix','w').write(n*'1 ' + '-\n' + n*'1 ' + '+\n')" \
  || die

# ...and extract the mutant names from mutants.log, just like the pipeline does
cut -f 1 -d ':' <"$MUTANTS_LOG" >"$DIR/mutant-names.txt" \
  || die

# Now run one branch of the pipeline to get the line-susps.

"$ANALYSIS_HOME/pipeline-scripts/crush-matrix" \
    --formula tarantula --total-defn tests --element-type Mutant \
    --matrix "$DIR/dummy.matrix" \
    --element-names "$DIR/mutant-names.txt" \
    --output "$DIR/dummy.mutant.susps" \
    || die

"$ANALYSIS_HOME/pipeline-scripts/aggregate-mutant-susps-by-stmt" \
    --accumulator avg \
    --mutants "$MUTANTS_LOG" \
    --mutant-susps "$DIR/dummy.mutant.susps" \
    --stmt-spans "$ANALYSIS_HOME/pipeline-scripts/source-code-lines/$P-1b.source-code.lines" \
    --output "$DIR/dummy.stmt.susps" \
    || die

"$ANALYSIS_HOME/pipeline-scripts/stmt-susps-to-line-susps" \
    --stmt-susps "$DIR/dummy.stmt.susps" \
    --source-code-lines "$ANALYSIS_HOME/pipeline-scripts/source-code-lines/$P-1b.source-code.lines" \
    --output "$DIR/dummy.line.susps" \
    || die



python3 -c "
import csv, collections, os

def is_junk(s):
  return (not s.strip()) or (s.strip() in '{}') or (s.strip().startswith('//'))

buggy_lines = {}
for line in open('$ANALYSIS_HOME/pipeline-scripts/buggy-lines/$P-$B.buggy.lines'):
  path, lno, source = line.strip().split('#', 2)
  if is_junk(source): continue
  buggy_lines[path+'#'+lno] = source

def is_foo(line):
  return (buggy_lines[line] == 'FAULT_OF_OMISSION')

ranked_lines = set(row['Line'] for row in csv.DictReader(open('$DIR/dummy.line.susps')))

candidates = collections.defaultdict(set)
if os.path.exists('$ANALYSIS_HOME/pipeline-scripts/buggy-lines/$P-$B.candidates'):
  for line in open('$ANALYSIS_HOME/pipeline-scripts/buggy-lines/$P-$B.candidates'):
    root, candidate = line.strip().split(',')
    candidates[root].add(candidate)

uncaught_lines = set(l for l in buggy_lines if l not in ranked_lines and not any(cand in ranked_lines for cand in candidates.get(l, [])))

summary = ''.join(l+'#'+buggy_lines[l]+'\n' for l in uncaught_lines)
open('$DIR/uncaught.lines','w').write(summary)
print(summary, end='')
"
