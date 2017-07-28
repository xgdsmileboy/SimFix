#!/bin/bash

die() {
  echo "$@" >&2
  exit 1
}

pushd "$FL_DATA_HOME/killmaps-unoptimized"

aws s3 sync s3://uw-suspense-fl/killmaps-unoptimized/ $FL_DATA_HOME/killmaps-unoptimized/ || die 'unable to pull killmaps from S3'

shopt -s globstar
for f in **/*.tar.gz; do
  [ -d "${f%.tar.gz}" ] || tar xf $f --strip-components 1
done

touch /tmp/_empty_file

for PROJECT in *; do
  [ -d $PROJECT ] || continue
  pushd $PROJECT || die "failed to cd into directory for $PROJECT"
  for BUG in *; do
    [ -d $BUG ] || continue
    pushd $BUG || die "failed to cd into directory for $PROJECT-$BUG"
    [ $BUG -ge 100000 ] && (ln -sf $PROJECT-${BUG::-5}f.source-code.lines $FL_DATA_HOME/analysis/pipeline-scripts/source-code-lines/$PROJECT-${BUG}b.source-code.lines || die "failed to symlink source-code lines file for $PROJECT-$BUG")

    [ -f scores.csv ] || $FL_DATA_HOME/analysis/pipeline-scripts/do-previously-studied-flts $PROJECT $BUG developer /tmp/_empty_file /tmp/_empty_file killmap.csv.gz mutants.log . scores.csv || die "failed to score unoptimized $PROJECT-$BUG"

    pushd "$(pwd | sed 's /killmaps-unoptimized/ /killmaps/ ')" || die "failed to switch into optimized-killmap directory for $PROJECT-$BUG"
    [ -f scores.csv ] || $FL_DATA_HOME/analysis/pipeline-scripts/do-previously-studied-flts $PROJECT $BUG developer /tmp/_empty_file /tmp/_empty_file killmap.csv.gz mutants.log . scores.csv || die "failed to score optimized $PROJECT-$BUG"
    popd

    popd
  done
  popd
done

popd

echo '####################################################'
echo '#                                                  #'
echo '#                                                  #'
echo '####################################################'

N_DIFFERENT=0
N_SAME=0
for f in killmaps-unoptimized/**/scores.csv; do
  echo "####################### ${f/killmaps-unoptimized//} #############################"
  if diff $f ${f/killmaps-unoptimized/killmaps/}; then
    ((N_SAME++))
  else
    ((N_DIFFERENT++))
  fi
done

echo "############## different:same = $N_DIFFERENT:$N_SAME ##################"
