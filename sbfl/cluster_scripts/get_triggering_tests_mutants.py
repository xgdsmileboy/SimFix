#!/usr/bin/python

import math
import sys
import os
import re
import random
import getpass

# Path to this script
SRC = os.path.dirname(sys.argv[0])
# Username running jobs
USERNAME = getpass.getuser()

if len(sys.argv) != 4:
  print "Usage:\n<nameOfScript>.py <dir> <mutantsFile> <maxJobs>"
  exit(1)

BASEDIR = os.path.abspath(sys.argv[1])
if not os.path.isdir(BASEDIR):
  print "creating folder: " + BASEDIR
  os.makedirs(BASEDIR)
else:
  print "target folder already exists"
  exit(1)

SCRIPTDIR = "%s/scripts" % BASEDIR
LOGDIR = "%s/logs" % BASEDIR
os.makedirs(SCRIPTDIR)
os.makedirs(LOGDIR)

# Initialize DB of target mutants
MUTANTS_FILE = sys.argv[2]
if not os.path.isfile(MUTANTS_FILE):
  print 'Could not find mutants file ' + sys.argv[2]
  exit(1)

MUTANTS = []
with open(MUTANTS_FILE) as f:
  for line in f:
    entry = line.rstrip().split(',')
    MUTANTS.append(entry)
  f.close()
NUM_MUTANTS=len(MUTANTS)

MAX_JOBS = int(sys.argv[3])
ENTRIES_PER_JOB = math.ceil(float(NUM_MUTANTS) / float(MAX_JOBS))

# Global counter of jobs created
JOB_ID=0

# --------------------------------------------------------------------

##
# Header to start all cluster jobs with
##
def getScriptHead():
  s =  "#!/bin/bash\n"
  s += "#$ -l h_rt=8:00:00\n"
  s += "#$ -l mem=4G\n"
  s += "#$ -l rmem=2G\n"
  s += "#$ -e /dev/null\n"
  s += "#$ -o /dev/null\n"
  s += "module load apps/java/1.7.0u55\n"
  s += "export MALLOC_ARENA_MAX=1\n"
  s += "export TZ='America/Los_Angeles'\n"
  s += "export _JAVA_OPTIONS=\"-XX:MaxHeapSize=256m -Xmx2048m\"\n"
  s += "export DEFECTS4J_HOME=%s\n" % os.environ['D4J_HOME']
  s += "export D4J_HOME=%s\n" % os.environ['D4J_HOME']

  return s

path_1 = "%s/%s_D4J_%d.sh" %(SCRIPTDIR, USERNAME, JOB_ID)
script = open(path_1, "a")
script.write(getScriptHead())

num=0
for entry in MUTANTS:
  if num >= ENTRIES_PER_JOB:
    script.close()

    JOB_ID += 1
    num = 1

    path_2 = "%s/%s_D4J_%d.sh" %(SCRIPTDIR, USERNAME, JOB_ID)
    script = open(path_2, "a")
    script.write(getScriptHead())
  else:
    num += 1

  project = entry[0]
  project = project.lstrip()
  project = project.rstrip()

  bug_id = entry[1]
  bug_id = bug_id.lstrip()
  bug_id = bug_id.rstrip()

  mutant_id = entry[2]
  mutant_id = mutant_id.lstrip()
  mutant_id = mutant_id.rstrip()

  log_dir="%s/%s/%s/%s" % (LOGDIR, project, bug_id, mutant_id)
  os.makedirs(log_dir)
  log_file="%s/log" % (log_dir)

  checkout_dir="/tmp/%s/d4j_artificial_faults-$$-%s-%s-%s" % (USERNAME, project, bug_id, mutant_id)

  commands = "\n"
  commands += "mkdir -p "+checkout_dir+" \n"
  commands += "$D4J_HOME/framework/bin/defects4j checkout -p "+project+" -v "+mutant_id+"b -w "+checkout_dir+" > "+log_file+" 2>&1 \n"

  commands += "pushd . >/dev/null \n"
  commands += "  cd %s \n" %(checkout_dir)
  commands += "  $D4J_HOME/framework/bin/defects4j compile >> %s 2>&1 \n" %(log_file)
  commands += "popd >/dev/null \n"

  commands += "$D4J_HOME/framework/bin/defects4j test -w "+checkout_dir+" >> "+log_file+" 2>&1 \n"
  commands += "mv -f "+checkout_dir+"/failing_tests $D4J_HOME/framework/projects/"+project+"/trigger_tests/"+mutant_id+" \n"
  commands += "rm -rf "+checkout_dir+" \n"
  commands += "\n"

  script.write(commands)
script.close()

print "Total number of jobs created: %d" % (JOB_ID+1)
print "Total number of calls to D4J: %d" % NUM_MUTANTS
print "Calls per job: %d" % ENTRIES_PER_JOB

# EOF

