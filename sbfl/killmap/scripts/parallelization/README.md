Scripts to identify which tests need to be run to complete a killmap.

If a killmap is taking a long time to finish running, you can use the script `list-unrun-nontriggering-tests-and-mutants.sh` to enumerate which ones remain to be run. The resulting CSV looks like:

        Test,Mutants
        mypackage.MyClass1#testMyMethod1,23 28 31 32 33 ...
        mypackage.MyClass1#testMyMethod2,23 28 31 32 33 ...
        mypackage.MyClass2#testMyMethod1,23 28 31 32 33 ...
        ...

This can be used to figure out which command line jobs will fill in the missing cells of the killmap, by doing something like

        PROJECT=Lang
        BUG=1
        PROJECT_DIR=/tmp/Lang-1b/
        KILLMAP=data/Lang/1/killmap.csv
        MUTANTS_LOG=data/Lang/1/mutants.log

        ./list-unrun-nontriggering-tests-and-mutants.sh $PROJECT_DIR $KILLMAP > unrun-tests-and-mutants.csv
        while IFS=, read TEST MUTANTS; do
          echo "killmap/scripts/generate-matrix.sh --no-preparation $PROJECT $BUG $PROJECT_DIR $MUTANTS_LOG --only-test-to-run $TEST --mutants-to-run $(sed 's/ /,/g' <<< $MUTANTS)"
        done < <(tail -n +2 unrun-tests-and-mutants.csv)

(The resulting killmap files can be stitched together using `$FL_DATA_HOME/analysis/pipeline-scripts/killmap-combiner.sh`.)


(`list-unrun-nontriggering-tests-and-mutants-with-pb.sh` does exactly the same thing as `list-unrun-nontriggering-tests-and-mutants.sh`, except it takes two extra arguments (a project name and a bug id), and its output has two more columns (`Project` and `Bug`).)
