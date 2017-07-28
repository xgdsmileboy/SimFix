#Statistical analyses

The scripts in this directory perform the statistical analyses described
in our report, using the scoring files in the [data](../data) directory.

# High-level summary
This summary indicates the purpose of each script.  Each script
provides a header with more detailed usage information and requirements.

* `analyze_best.R`: Computes the top-25 FL techniques, and compares the
                     best FL technique with all others (considering
                     different debugging scenarios and EXAM score vs.
                     FLT rank).

* `anova.R`: Performs an ANOVA and post-hoc Tukey test.

* `boxplot.R`: Visualizes the distribution of EXAM scores for all
               previously studied techniques for real and artificial
               faults.

* `compNewTechniques.R`: Compares our new techniques with existing
                         techniques on artificial and real faults.

* `muse.R`: Plots the distributions of the EXAM score and absolute score
            for each kill definition for MUSE.

* `newTechniques.R`: Plots the distributions of the EXAM score and
                     absolute score of our new techniques. 

* `relativeRelationship.R`: Computes the correlation between FLT ranks
                            for real faults and artificial faults.

* `replication.R`: Replicates prior studies by comparing existing
                   techniques on artificial and real faults, using the
                   best-case debugging scenario.

* `replication_all_scoring_schemes.R`: Performs the same analysis as
                                       `replication.R` for all debugging
                                       scenarios.

* `top-n.R`: Computes the ratio of defects that the best existing mbfl
             and sbfl techniques, and our new techniques techniques
             localize in the top-5, top-10, and top-200.

* `sbfl_runtime.R`: Generates latex macros of SBFL's runtime.

* `mbfl_runtime.R`: Generates latex macros of MBFL technique.

* `util.R`: Helper functions used in several scripts.
