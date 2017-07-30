#!/usr/bin/python
import sys
import os
from cluster_util import *

#
# Generate all cluster jobs to produce data for generate test suites
#


SEEDS_PER_JOB = 1

if len(sys.argv) != 6:
    print "Usage:\n%s <scriptdir> <testdir> <minSeed> <maxSeed> <datadir>" % sys.argv[0]
    exit(1)

# Path to this script
SRC=os.path.abspath(os.path.dirname(sys.argv[0]))

SCRIPTDIR = os.path.abspath(sys.argv[1])
TESTDIR   = os.path.abspath(sys.argv[2])

if not os.path.isdir(SCRIPTDIR):
    print "creating folder: " + SCRIPTDIR
    os.makedirs(SCRIPTDIR)
else:
    print "target folder already exists"
    exit(1)

MINSEED = int(sys.argv[3])
MAXSEED = int(sys.argv[4])
DATADIR = sys.argv[5]

DEFECTS4J=os.environ['DEFECTS4J_HOME']

#
# Generate test suite with EvoSuite for one bug
#
def generate_evosuite(project, bug, seed, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global USERNAME
    
    script = "%s/framework/bin/run_evosuite.pl" % DEFECTS4J
    tmpdir="/scratch/%s/%s/%s/%s" % (USERNAME, project, bug, seed)
    outdir="%s/tests_evosuite/%s/%s" % (TESTDIR, project, bug)
    call = "if [ ! -e \"%s/%s/evosuite/%d\" ]; then\n" % (outdir, project, seed)
    call +=   "%s -A -p %s -v %s%s -n %d -o %s -t %s\n" % (script, project, bug, pass_fail, seed, outdir, tmpdir)
    call += "fi\n"
    return call
    
#
# Generate test suite with Randoop for one bug
#
def generate_randoop(project, bug, seed, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global USERNAME
    
    script = "%s/framework/bin/run_randoop.pl" % DEFECTS4J
    tmpdir="/scratch/%s/%s/%s/%s" % (USERNAME, project, bug, seed)
    outdir="%s/tests_randoop/%s/%s" % (TESTDIR, project, bug)
    call = "if [ ! -e \"%s/%s/randoop/%d\" ]; then\n" % (outdir, project, seed)
    call +=   "%s -p %s -v %s%s -b 120 -n %d -o %s -t %s\n" % (script, project, bug, pass_fail, seed, outdir, tmpdir)
    call += "fi\n"
    return call

#
# Fix one EvoSuite test suite
#
def fix_evosuite(project, bug, seed, pass_fail='f'):
    return run_fix(project, bug, seed, "evosuite", "*Test.java", pass_fail)

#
# Fix one Randoop test suite
#
def fix_randoop(project, bug, seed, pass_fail='f'):
    return run_fix(project, bug, seed, "randoop", "RegressionTest*.java", pass_fail)

#
# Internal fix call
#
def run_fix(project, bug, seed, testname, pattern, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global USERNAME
    
    script = "%s/framework/util/fix_test_suite.pl -f \"%s\"" % (DEFECTS4J, pattern)
    tmpdir="/scratch/%s/%s/%s/%s" % (USERNAME, project, bug, seed)
    testdir="%s/tests_%s/%s/%s/%s/%s/%d" % (TESTDIR, testname, project, bug, project, testname, seed)

    call = "if [ ! -e \"%s/fix_test_suite.log\" ]; then\n" % (testdir)
    call += "%s -p %s -v %s%s -d %s -t %s\n" % (script, project, bug, pass_fail, testdir, tmpdir)
    call += "fi\n"
    return call

#
# Determine if bugs are detected by an evosuite test suite
#
def run_evosuite(project, bug, seed, datadir, pass_fail='f'):
    return run_bug_detection(project, bug, seed, "evosuite", pass_fail)

#
# Determine if bugs are detected by a Randoop test suite
#
def run_randoop(project, bug, seed, datadir, pass_fail='f'):
    return run_bug_detection(project, bug, seed, "randoop", pass_fail)

#
# Determine if bugs are detected
#
def run_bug_detection(project, bug, seed, testname, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global DATADIR
    global USERNAME
    
    script = "%s/framework/bin/run_bug_detection.pl" % DEFECTS4J
    tmpdir="/scratch/%s/%s/%s/%s" % (USERNAME, project, bug, seed)
    outdir = "%s/%s/%s/%d" % (DATADIR, project, bug, seed)
    testdir="%s/tests_%s/%s/%s/%s/%s/%d" % (TESTDIR, testname, project, bug, project, testname, seed)
    return "%s -p %s -v %s%s -d %s -t %s -o %s" % (script, project, bug, pass_fail, testdir, tmpdir, outdir)


def run_killmap_randoop(project, bug, seed, testname, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global DATADIR
    global USERNAME

    KILLMAP=os.environ['KILLMAP_HOME']
    tmpdir="/scratch/%s/%s/%s/%s/killmap" % (USERNAME, project, bug, seed)
    outfile = "%s/%s/%s.killmap" % (DATADIR, project, bug)
    outdir = "%s/%s/%s/%d" % (DATADIR, project, bug, seed)
    testdir="%s/tests_%s/%s/%s" % (TESTDIR, testname, project, bug)
    s = "mkdir -p %s/%s\n" % (DATADIR, project)
    s += "mkdir -p %s\n" % (tmpdir)
    suite = "%s/%s/randoop/%d/%s-%sf-randoop.%d.tar.bz2" % (testdir, project, seed, project, bug, seed)
    s += "%s/run_killmap_randoop.sh %s %s\n" % (SRC, suite, DATADIR)

    s += "rm -rf %s\n" % tmpdir
    return s

def run_killmap_evosuite(project, bug, seed, testname, pass_fail='f'):
    global TESTDIR
    global DEFECTS4J
    global DATADIR
    global USERNAME

    KILLMAP=os.environ['KILLMAP_HOME']
    tmpdir="/scratch/%s/%s/%s/%s/killmap" % (USERNAME, project, bug, seed)
    outfile = "%s/%s/%s.killmap" % (DATADIR, project, bug)
    outdir = "%s/%s/%s/%d" % (DATADIR, project, bug, seed)
    testdir="%s/tests_%s/%s/%s" % (TESTDIR, testname, project, bug)
    s = "mkdir -p %s/%s\n" % (DATADIR, project)
    s += "mkdir -p %s\n" % (tmpdir)
    suite = "%s/%s/evosuite/%d/%s-%sf-evosuite.%d.tar.bz2" % (testdir, project, seed, project, bug, seed)
    s += "%s/run_killmap_evosuite.sh %s %s\n" % (SRC, suite, DATADIR)

    s += "rm -rf %s\n" % tmpdir
    return s

#
# Create jobs that call target on all bugs/projects
#
def create_jobs(bugs, call, scriptdir, minseed, maxseed):
    global SEEDS_PER_JOB
    os.makedirs(scriptdir)
    
    for project in bugs.keys():
        for bug in bugs[project]:
            num = 0
            job_id = 0
            path = "%s/%s_%s_%d.sh" %(scriptdir, project, bug, job_id)
            script=open(path, "a")
            script.write(get_script_head())
            for seed in range(minseed, maxseed):
                if num >= SEEDS_PER_JOB:
                    script.close()
                    job_id +=1
                    num = 1 
                    path_2 = "%s/%s_%s_%d.sh" %(scriptdir, project, bug, job_id)
                    script=open(path_2, "a")
                    script.write(get_script_head())
                else:
                    num += 1

                script.write(call(project, bug, seed, 'f'))
                script.write("\n")
            script.close()


bugs = get_bugs()

create_jobs(bugs, run_randoop, "%s/scripts_run_randoop" % SCRIPTDIR, MINSEED, MAXSEED)
create_jobs(bugs, fix_randoop, "%s/scripts_fix_randoop" % SCRIPTDIR, MINSEED, MAXSEED)
create_jobs(bugs, run_evosuite, "%s/scripts_run_evosuite" % SCRIPTDIR, MINSEED, MAXSEED)
create_jobs(bugs, fix_evosuite, "%s/scripts_fix_evosuite" % SCRIPTDIR, MINSEED, MAXSEED)
create_jobs(bugs, run_killmap_randoop,   "%s/scripts_killmap_randoop" % SCRIPTDIR,   MINSEED, MAXSEED)
create_jobs(bugs, run_killmap_evosuite,  "%s/scripts_killmap_evosuite" % SCRIPTDIR,  MINSEED, MAXSEED)



