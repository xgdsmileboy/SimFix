import re
import collections
import csv

def java_classname_to_path(classname):
  return re.sub(r'\$[^#]*', '', classname).replace('.', '/') + '.java'
def path_to_java_classname(path):
  return path[:-len('.java')].replace('/','.')

MUTANT_LOG_LINE_PATTERN = re.compile(r'''
  (?P<id>\d+):
  (?P<mutant_type>[^:]*):
  (?P<abstract_lhs>.*?):
  (?P<abstract_rhs>[^:]*):
  (?P<classname>[a-zA-Z0-9_.]+)[^:]*:
  (?P<lineno>\d+):
  (?P<lhs>.*?)\ \|==>\ #
  (?P<rhs>.*)$
''', flags=re.X)

Mutant = collections.namedtuple('Mutant', 'id mutant_type abstract_lhs abstract_rhs classname lineno lhs rhs')
def iter_mutants_log_lines(file):
  '''Iterate over every mutant described in one of Major's mutants.log files.'''
  for line in file:
    if MUTANT_LOG_LINE_PATTERN.match(line.strip()) is None:
      print(repr(line))
      import pdb; pdb.set_trace()

    mutant = Mutant(**MUTANT_LOG_LINE_PATTERN.match(line.strip()).groupdict())
    yield mutant._replace(id=int(mutant.id), lineno=int(mutant.lineno))

KillmapTestRun = collections.namedtuple('KillmapTestRun', 'test mutant_id timeout category time hash covered_mutant_ids traceback')
def iter_killmap_test_runs(file):
  '''Iterate over every `KillmapTestRun` described in a killmap file.'''
  for line in file:
    run = KillmapTestRun(*line.strip().split(',', 7))
    yield run._replace(
      mutant_id=int(run.mutant_id),
      timeout=int(run.timeout),
      time=int(run.time),
      covered_mutant_ids=set(int(m) for m in run.covered_mutant_ids.split(' ')) if run.covered_mutant_ids else set())

FLTScoreEval = collections.namedtuple('FLTScoreEval', 'project bug test_suite scoring_scheme family formula total_defn kill_defn hybrid_scheme aggregate_defn score score_wrt_loaded_classes')
def _flt_score_eval_to_csv_row_dict(e):
  return {
    'Project': e.project, 'Bug': str(e.bug), 'TestSuite': e.test_suite, 'ScoringScheme': e.scoring_scheme,
    'Family': e.family, 'Formula': e.formula, 'TotalDefn': e.total_defn, 'KillDefn': e.kill_defn, 'HybridScheme': e.hybrid_scheme, 'AggregationDefn': e.aggregate_defn,
    'Score': str(e.score), 'ScoreWRTLoadedClasses': str(e.score_wrt_loaded_classes)}
def _csv_row_dict_to_flt_score_eval(row):
  return FLTScoreEval(
    project=row['Project'],
    bug=int(row['Bug']),
    test_suite=row['TestSuite'],
    scoring_scheme=row['ScoringScheme'],
    family=row['Family'],
    formula=row['Formula'],
    total_defn=row['TotalDefn'],
    kill_defn=row['KillDefn'],
    hybrid_scheme=row['HybridScheme'],
    aggregate_defn=row['AggregationDefn'],
    score=float(row['Score']),
    score_wrt_loaded_classes=float(row['ScoreWRTLoadedClasses']))
def iter_flt_score_evals(file):
  '''Iterate over all of the scores in a FLT-score file (e.g. those in the /data dir). Yields results as `FLTScoreEval`s.'''
  for row in csv.DictReader(file):
    yield _csv_row_dict_to_flt_score_eval(row)
def write_flt_score_evals(file, evals):
  '''Write all the given `FLTScoreEval`s to a file.'''
  writer = csv.DictWriter(file, fieldnames=['Project', 'Bug', 'TestSuite', 'ScoringScheme', 'Family', 'Formula', 'TotalDefn', 'KillDefn', 'HybridScheme', 'AggregationDefn', 'Score', 'ScoreWRTLoadedClasses'])
  writer.writeheader()
  for e in evals:
    writer.writerow(_flt_score_eval_to_csv_row_dict(e))

Line = collections.namedtuple('Line', ['path', 'lineno'])
def parse_line(line):
  path, lineno = line.split('#')
  return Line(path, int(lineno))

def get_buggy_lines(file):
  '''Parse a buggy-lines file into a set of `Line`s.'''
  result = set()
  for line in file:
    if isinstance(line, bytes):
      path, lineno, source_code = line.strip().split(b'#', 2)
      path = path.decode()
    else:
      path, lineno, source_code = line.strip().split('#', 2)
    result.add(Line(path, int(lineno)))
  return result

def parse_candidate_lines(file):
  '''Parse a buggy-line-candidate file into a dict, mapping buggy `Line`s to candidate `Line`s.'''
  result = collections.defaultdict(set)
  for buggy_line, candidate in csv.reader(file):
    result[parse_line(buggy_line)].add(parse_line(candidate))
  return result
