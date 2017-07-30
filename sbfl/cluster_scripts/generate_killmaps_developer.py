#!/usr/bin/python
import sys
import os
from cluster_util import *

#
# Generate cluster jobs to produce killmaps for developer written tests
#

if len(sys.argv) != 3:
    print "Usage:\n%s <scriptdir> <datadir>" % sys.argv[0]
    exit(1)

SCRIPTDIR = os.path.abspath(sys.argv[1])

if not os.path.isdir(SCRIPTDIR):
    print "creating folder: " + SCRIPTDIR
    os.makedirs(SCRIPTDIR)
else:
    print "target folder already exists"
    exit(1)

DATADIR = sys.argv[2]

DEFECTS4J=os.environ['DEFECTS4J_HOME']

#
# Run killmap on one specific bug
#
def run_killmap_devtests(project, bug):
    global USERNAME
    global TESTDIR
    global DEFECTS4J
    global DATADIR

    KILLMAP=os.environ['KILLMAP_HOME']
    tmpdir="/scratch/%s/killmap/%s/%s" % (USERNAME, project, bug)

    outdir = "%s/%s/killmaps/%s/%s" % (DATADIR, project, project, bug)
    killmap_csv = "%s/killmap.csv" %(outdir)
    log = "%s/log.txt" %(outdir)
    mutants_log = "%s/mutants.log" %(outdir)

    s = "mkdir -p %s\n" % (tmpdir)
    s += "mkdir -p %s\n" % (outdir)
    s += "%s/scripts/generate-matrix.sh %s %s %s %s 2>%s > %s\n" % (KILLMAP, project, bug, tmpdir, mutants_log, log, killmap_csv)

    s += "echo \"[INFO] Checking generated files\" >> %s 2>&1\n" %(log)
    s += "if [ ! -s \"$killmap_csv\" ]; then\n"
    s += "  echo \"[ERROR] (%s-%s) '%s' does not exist or is empty!\" >> %s 2>&1\n" %(project, bug, killmap_csv, log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"

    s += "if [ ! -s \"$mutants_log\" ]; then\n"
    s += "  echo \"[ERROR] (%s-%s) '%s' does not exist or is empty!\" >> %s 2>&1\n" %(project, bug, mutants_log, log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"

    s += "if ! grep -q \"^Completed successfully\!$\" \"%s\"; then\n" %(log)
    s += "  echo \"[ERROR] (%s-%s) KillMap did not finished successfully!\" >> %s 2>&1\n" %(project, bug, log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"

    s += "echo \"[INFO] Compressing '%s' to '%s.gz'\" >> %s 2>&1\n" %(killmap_csv, killmap_csv, log)
    s += "cat \"%s\" | gzip > \"%s.gz\"\n" %(killmap_csv, killmap_csv)
    s += "if [ $? -ne 0 ]; then\n"
    s += "  echo \"[ERROR] (%s-%s) Compression of '%s' failed!\" >> %s 2>&1\n" %(project, bug, killmap_csv, log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"

    s += "echo \"[INFO] Checking if '%s.gz' is complete\" >> %s 2>&1\n" %(killmap_csv, log)
    s += "sanity_check_log=$(python \"%s/scripts/sanity-check\" \"%s.gz\")\n" %(KILLMAP, killmap_csv)
    s += "if [ $? -ne 0 ]; then\n"
    s += "  echo \"[ERROR] sanity-check of '%s.gz' failed!\" >> %s 2>&1\n" %(killmap_csv, log)
    s += "  echo \"$sanity_check_log\" >> %s 2>&1\n" %(log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"
    s += "if [ \"$sanity_check_log\" != \"\" ]; then\n"
    s += "  echo \"[ERROR] sanity-check of '%s.gz' reported:\" >> %s 2>&1\n" %(killmap_csv, log)
    s += "  echo \"$sanity_check_log\" >> %s 2>&1\n" %(log)
    s += "  rm -rf \"%s\" >> %s 2>&1\n" %(tmpdir, log)
    s += "  exit 1;\n"
    s += "fi\n"

    s += "pushd . >/dev/null\n"
    s += "cd \"%s/%s\"\n" %(DATADIR, project)
    s += "  echo \"[INFO] Compressing all files from 'killmaps/%s/%s/' to '%s.tar.gz'\" >> %s 2>&1\n" %(project, bug, bug, log)
    s += "  echo \"DONE!\" >> %s 2>&1\n" %(log) # for now on, do not print any message to the log file!
    s += "  tar -czf \"%s.tar.gz\" \"killmaps/%s/%s/killmap.csv.gz\" \"killmaps/%s/%s/log.txt\" \"killmaps/%s/%s/mutants.log\"\n" %(bug, project, bug, project, bug, project, bug)
    s += "popd >/dev/null\n"

    s += "rm -rf %s\n" %(tmpdir)
    return s

#
# Create jobs that call target on all bugs/projects
#
def create_jobs(scriptdir):
       
    bugs = get_bugs() 
    for project in bugs.keys():
        for bug in bugs[project]:
            num = 0
            job_id = 0
            path = "%s/%s_%s_%d.sh" %(scriptdir, project, bug, job_id)
            script=open(path, "a")
            script.write(get_script_head())
            script.write(run_killmap_devtests(project, bug))
            script.write("\n")
            script.close()


create_jobs(SCRIPTDIR)
