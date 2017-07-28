#!/bin/bash
#$ -l h_rt=8:00:00
#$ -l mem=4G
#$ -l rmem=2G
module load apps/java/1.7.0u55
export MALLOC_ARENA_MAX=1
export _JAVA_OPTIONS="-XX:MaxHeapSize=256m -Xmx1024m"

# get node name. if something goes wrong this will be useful to inform
# iceberg's admins
hostname;

#
# Print error message and exit
#
die() {
  echo $1
  exit 1
}

# Check whether D4J_HOME is set
[ "$D4J_HOME" != "" ] || die "D4J_HOME is not set!"
# Check whether JAVA_PARSER_JAR is set
[ "$JAVA_PARSER_JAR" != "" ] || die "JAVA_PARSER_JAR is not set!"

EXT="source-code.lines"

##
# Arguments
pid=$1
bid=$2
output_dir=$3

MUTANTS_IN_SCOPE="$D4J_HOME/framework/projects/$pid/mutants_in_scope.csv"

# get BUGGY and FIXED
for bf in b f; do

  # Set temporary directory used to checkout the project versions
  TMP_DIR="/tmp/$USER/run_java-parser_$$-$pid-${bid}${bf}"
  rm -rf $TMP_DIR
  mkdir -p $TMP_DIR

  echo "* Checking out $pid-${bid}${bf}"
  $D4J_HOME/framework/bin/defects4j checkout -p $pid -v ${bid}${bf} -w $TMP_DIR || die "Checkout failed!"

  pushd . > /dev/null 2>&1 
  cd $TMP_DIR
    echo "* Getting dir.src.classes path"
    DIR_SRC_CLASSES=$($D4J_HOME/framework/bin/defects4j export -p dir.src.classes)
    DIR_SRC_CLASSES="$TMP_DIR/$DIR_SRC_CLASSES"
  popd > /dev/null 2>&1

  # Collect loaded classes and convert new line "\n" to ":"
  DIR_LOADED_CLASSES="$D4J_HOME/framework/projects/$pid/loaded_classes"
  LOADED_CLASSES=$(cat "$DIR_LOADED_CLASSES/$bid.src" | tr '\n' ':')

  echo "* Parsing all loaded-classes"
  java -jar "$JAVA_PARSER_JAR" \
      $DIR_SRC_CLASSES \
      $LOADED_CLASSES \
      "$output_dir/$pid-${bid}${bf}.$EXT" || die "Parsing failed!"

  if [ -f "$output_dir/$pid-${bid}${bf}.$EXT" ]; then
    num_lines=`wc -l "$output_dir/$pid-${bid}${bf}.$EXT" | cut -f1 -d' '`
    if [ "$num_lines" -eq 0 ]; then
      echo "[WARN] File $output_dir/$pid-${bid}${bf}.$EXT is empty!"
    fi
  else
    die "There is not any $output_dir/$pid-${bid}${bf}.$EXT file!"
  fi

  # Remove temporary directory
  rm -rf $TMP_DIR
done

echo "DONE!"

# EOF

