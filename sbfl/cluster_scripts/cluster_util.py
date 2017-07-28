#!/usr/bin/python
import sys
import os
import csv

DEFECTS4J=os.environ['DEFECTS4J_HOME']
USERNAME=getpass.getuser()

#
# Generate a dictionary of project name -> list of bugs
#
def get_bugs():
    projects = next(os.walk("%s/framework/projects" % DEFECTS4J))[1]
    projects.remove("lib")

    bugs = {}

    for project in projects:
        bugs[project] = []
        commitdb_file = open("%s/framework/projects/%s/commit-db" % (DEFECTS4J, project))
        scope_file = open("%s/framework/projects/%s/mutants_in_scope.csv" % (DEFECTS4J, project))
        csv_reader = csv.reader(scope_file)
        artificial_faults_in_scope = [i[2] for i in csv_reader]
        lines = [line.rstrip('\n') for line in commitdb_file]
        for line in lines:
            (bugid,_,_) = line.split(',')
            if int(bugid) > 1000 and bugid not in artificial_faults_in_scope:
                # artificial fault is not in scope!
                continue
            bugs[project].append(bugid)
    return bugs

#
# Header to start all cluster jobs with
#
def get_script_head():
    s =  "#!/bin/bash\n"
    s += "#$  -l h_rt=32:00:00\n"
    s += "#$  -l mem=8G\n"
    s += "#$  -l rmem=4G\n"
    s += "module load apps/java/1.7.0u55\n"
    s += "module load apps/python/2.7\n"
    s += "export MALLOC_ARENA_MAX=1\n"
    s += "export _JAVA_OPTIONS=-Xmx1500M\n"
    s += "export DEFECTS4J_HOME=%s\n" % os.environ['DEFECTS4J_HOME']
    s += "export D4J_HOME=%s\n" % os.environ['DEFECTS4J_HOME']
    s += "export ANALYSIS_HOME=%s\n" % os.environ['ANALYSIS_HOME']

    return s

SEEDS_PER_JOB=1

#
# Create jobs that call target on all bugs/projects
#
def create_jobs(call, scriptdir, min_seed, max_seed):
    global SEEDS_PER_JOB
    bugs = get_bugs()
        
    for project in bugs.keys():
        for bug in bugs[project]:
            num = 0
            job_id = 0
            path = "%s/%s_%s_%d.sh" %(scriptdir, project, bug, job_id)
            script=open(path, "a")
            script.write(get_script_head())
            for seed in range(min_seed, max_seed):
                if num >= SEEDS_PER_JOB:
                    script.close()
                    job_id +=1
                    num = 1 
                    path_2 = "%s/%s_%s_%d.sh" %(scriptdir, project, bug, job_id)
                    script=open(path_2, "a")
                    script.write(get_script_head())
                else:
                    num += 1

                script.write(call(project, bug, seed))
                script.write("\n")
            script.close()


