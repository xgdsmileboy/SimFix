#!/usr/bin/python
import sys
import os
from cluster_util import *

if len(sys.argv) != 3:
    print "Usage:\n%s <script dir> <output dir>" % sys.argv[0]
#    exit(1)

# The directory in which the jobs will be written
SCRIPTDIR = os.path.abspath(sys.argv[1])

# The source directory where coverage and killmaps are found and scores will be written
DATADIR   = os.path.abspath(sys.argv[2])

if not os.path.isdir(SCRIPTDIR):
    print "creating folder: " + SCRIPTDIR
    os.makedirs(SCRIPTDIR)
else:
    print "target folder already exists"
    exit(1)

DEFECTS4J=os.environ['DEFECTS4J_HOME']

def do_pipeline_randoop(project, bug, seed):
    global DATADIR
    global USERNAME

    script = "%s/pipeline-scripts/do-full-analysis" % os.environ['ANALYSIS_HOME']
    tmpdir="/scratch/%s/pipeline_randoop_%s_%s" % (USERNAME, project, bug)
    outdir="%s/scores/randoop/%s/%s" % (DATADIR, project, bug)
    call = "if [ ! -e \"%s/scores.csv\" ]; then\n" % (outdir)
    call += "  if [ ! -e \"%s/mutation_files/randoop/%s/%s/%s.killmap.gz\" ]; then\n" % (DATADIR, project, bug, bug)
    call += "    echo \"No killmap, skipping\"\n"
    call += "    exit\n"
    call += "  fi\n"
    call += "  mkdir -p %s\n" % tmpdir
    call += "  mkdir -p %s\n" % outdir
    call += "  pushd . > /dev/null\n"
    call += "  cd %s\n" % tmpdir
    call += "  cp %s/mutation_files/randoop/%s/%s/%s.killmap.gz .\n" % (DATADIR, project, bug, bug)
    call += "  cp %s/gzoltar_files/randoop/%s/%s/matrix.gz .\n" % (DATADIR, project, bug)
    call += "  cp %s/gzoltar_files/randoop/%s/%s/spectra.gz .\n" % (DATADIR, project, bug)
    call += "  cp %s/mutation_files/randoop/%s/%s/mutants.log .\n" % (DATADIR, project, bug)
    call += "  gzip -d %s.killmap.gz\n" % (bug)
    call += "  gzip -d matrix.gz\n"
    call += "  gzip -d spectra.gz\n"
    gzoltar_matrix     = "%s/matrix" % (tmpdir)
    gzoltar_statements = "%s/spectra"  % (tmpdir)
    mutant_log         = "%s/mutants.log" % (tmpdir)
    killmap            = "%s/%s.killmap" % (tmpdir, bug)
    call += "  %s %s %s randoop %s %s %s %s . %s/scores.csv\n" % (script, project, bug, gzoltar_matrix, gzoltar_statements, killmap, mutant_log, outdir)
    call += "  popd > /dev/null\n"
    call += "  rm -rf %s\n" % tmpdir
    call += "else\n"
    call += "  echo \"Skipping %s/%s/scores.csv as it already exists\"\n" % (outdir, project)
    call += "fi\n"
    return call

create_jobs(do_pipeline_randoop, SCRIPTDIR, 0, 1)

