#!/usr/bin/python

import os
import csv
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('faults_csv')
parser.add_argument('targets_dir')
args = parser.parse_args()

with open(args.faults_csv) as faults_file:
  for project, bug in csv.reader(faults_file):
    targets_filename = os.path.join(args.targets_dir, project, bug+'.csv')
    os.makedirs(os.path.dirname(targets_filename), exist_ok=True)
    with open(targets_filename, 'w') as targets_file:
      writer = csv.writer(targets_file, lineterminator='\n')
      writer.writerow(('Project','Bug','Timeout'))
      writer.writerow((project, bug, '36h'))
