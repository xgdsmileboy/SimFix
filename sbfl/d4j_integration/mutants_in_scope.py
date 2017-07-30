#!/usr/bin/python

'''Describes all the mutants in a set of buggy statements.

Requires, as input:
- a mutants.log file produced by Major (presumably from mutating the
  fixed program version);
- a stmt-spans file (i.e. a `.source-code.lines` file) listing which lines
  each statement spills over onto; and
- a file listing the lines fixed by the patch, e.g. "mypackage/MyClass.java#123".

Prints to stdout: a CSV detailing which mutants are in the same statements as
any fixed lines.

Example:

    $ python d4j_integration/mutants_in_scope.py \\
        /tmp/scoping/Chart/13/mutants.log \\
        analysis/java-parser/source-code-lines/Chart-13f.source-code.lines \\
        analysis/pipeline-scripts/fixed-lines/Chart-13.fixed.lines
    id,line,mutant_type,abstract_lhs,abstract_rhs,lhs,rhs
    1819,org/jfree/chart/block/BorderArrangement.java#454,LVR,0,POS,0.0,1.0
    1820,org/jfree/chart/block/BorderArrangement.java#454,LVR,0,NEG,0.0,-1.0
    1821,org/jfree/chart/block/BorderArrangement.java#455,LVR,0,POS,0.0,1.0
    1822,org/jfree/chart/block/BorderArrangement.java#455,LVR,0,NEG,0.0,-1.0
    1823,org/jfree/chart/block/BorderArrangement.java#455,LVR,POS,0,2,0
    1824,org/jfree/chart/block/BorderArrangement.java#455,LVR,POS,NEG,2,-2
    1825,org/jfree/chart/block/BorderArrangement.java#455,AOR,"-(double,double)","%(double,double)",constraint.getWidth() - w[2],constraint.getWidth() % w[2]
    1826,org/jfree/chart/block/BorderArrangement.java#455,AOR,"-(double,double)","*(double,double)",constraint.getWidth() - w[2],constraint.getWidth() * w[2]
    1827,org/jfree/chart/block/BorderArrangement.java#455,AOR,"-(double,double)","+(double,double)",constraint.getWidth() - w[2],constraint.getWidth() + w[2]
    1828,org/jfree/chart/block/BorderArrangement.java#455,AOR,"-(double,double)","/(double,double)",constraint.getWidth() - w[2],constraint.getWidth() / w[2]
    1829,org/jfree/chart/block/BorderArrangement.java#455,LVR,0,POS,0.0,1.0
    1830,org/jfree/chart/block/BorderArrangement.java#455,LVR,0,NEG,0.0,-1.0
    1831,org/jfree/chart/block/BorderArrangement.java#456,LVR,POS,0,2,0
    1832,org/jfree/chart/block/BorderArrangement.java#456,LVR,POS,NEG,2,-2

'''

import re
import csv
import sys
import pyfl

if __name__ == '__main__':
  import argparse
  parser = argparse.ArgumentParser(description='Prints a CSV describing all the mutants in the buggy lines.')
  parser.add_argument('mutants_log')
  parser.add_argument('stmt_spans_file')
  parser.add_argument('fixed_lines_file')
  args = parser.parse_args()

  with open(args.mutants_log) as f:
    mutants = pyfl.formats.iter_mutants_log_lines(f)
    writer = csv.DictWriter(sys.stdout, fieldnames=['id', 'line', 'mutant_type', 'abstract_lhs', 'abstract_rhs', 'lhs', 'rhs'])
    writer.writeheader()
    for mutant in pyfl.iter_mutants_in_scope(mutants=mutants, stmt_spans_file=args.stmt_spans_file, fixed_lines_file=args.fixed_lines_file):
      writer.writerow({
        'id': mutant.id,
        'line': pyfl.get_mutant_path_and_line(mutant),
        'mutant_type': mutant.mutant_type,
        'abstract_lhs': mutant.abstract_lhs,
        'abstract_rhs': mutant.abstract_rhs,
        'lhs': mutant.lhs,
        'rhs': mutant.rhs})
