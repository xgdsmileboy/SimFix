#!/bin/bash

set -e

sudo yum update --assumeyes
sudo yum install --assumeyes ant git svn maven

git clone https://github.com/speezepearson/defects4j.git
pushd defects4j
  ./init.sh
  export DEFECTS4J_HOME=$(pwd)
  export D4J_HOME=$(pwd)
  echo "export DEFECTS4J_HOME='$(pwd)'" >> ~/.bashrc
  echo "export D4J_HOME='$(pwd)'" >> ~/.bashrc
popd

git clone 'https://bitbucket.org/rjust/fault-localization-data.git'
pushd fault-localization-data
  echo "export FL_DATA_HOME='$(pwd)'" >> ~/.bashrc
  pushd killmap
    ant compile
    echo "export KILLMAP_HOME='$(pwd)'" >> ~/.bashrc
  popd
popd
