#!/bin/bash

HERE=$(dirname "$(readlink --canonicalize "${BASH_SOURCE[0]}")")

die() {
  echo "$@" >&2
  exit 1
}

set-env-var-forever() {
  CODE="export $1='$2'"
  eval "$CODE"
  fgrep -q "$CODE" ~/.bashrc || echo "$CODE" >> ~/.bashrc
}

echo 'Adding environment variables to bashrc...' >&2
echo >> ~/.bashrc || die 'unable to write to ~/.bashrc'
echo "#### added by $(readlink --canonicalize "${BASH_SOURCE[0]}")" >> ~/.bashrc
set-env-var-forever FL_DATA_HOME "$HERE"
set-env-var-forever KILLMAP_HOME "$HERE/killmap/"

if [ ! -d "$D4J_HOME" ]; then
  git clone 'https://github.com/speezepearson/defects4j.git' "$HERE/defects4j" || die 'unable to clone Defects4J'
  (cd "$HERE/defects4j" && ./init.sh) || die 'unable to initialize Defects4J'
  set-env-var-forever D4J_HOME "$HERE/defects4j/"
  set-env-var-forever DEFECTS4J_HOME "$HERE/defects4j/"
  echo 'export PATH="$PATH:$D4J_HOME/framework/bin"' >> ~/.bashrc
fi

GZOLTAR_JAR_URL="http://www.gzoltar.com/lib/com.gzoltar-1.6.0-jar-with-dependencies.jar"
GZOLTAR_JAR_FILE="$HERE/gzoltar/gzoltar.jar"
if [ ! -f "$GZOLTAR_JAR_FILE" ]; then
  wget -np -nv "$GZOLTAR_JAR_URL" -O "$GZOLTAR_JAR_FILE" || die 'unable to get GZoltar jar file'
fi
set-env-var-forever GZOLTAR_JAR "$GZOLTAR_JAR_FILE"

echo 'Unzipping tarball mapping line numbers to statement-start-lines...'
(cd "$HERE/analysis/pipeline-scripts" && tar xf source-code-lines.tar.gz) || die 'unable to unzip analysis/pipeline-scripts/source-code-lines.tar.gz'

echo 'Compiling Killmap...'
(cd "$KILLMAP_HOME" && ant compile) || die 'unable to compile Killmap'

echo 'Done! You need to `source ~/.bashrc` or start a new shell session before things will work right, though.' >&2
