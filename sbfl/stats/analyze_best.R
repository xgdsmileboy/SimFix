# Script that generates LaTex tables for the top-25 FL techniques and the comparison of the
# best FL technique with all others, considering
# different scoring schemes, aggregation functions, and score vs. rank values.
#
# usage: Rscript analyze_best.R <data_file> <out_dir>
#

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
data_file <- args[1]
out_dir <- args[2]

# Check number of arguments
if (length(args)!=2) {
    stop("usage: Rscript analyze_best.R <data_file> <out_dir>")
}

# Collection of helper functions
source("util.R")
require("effsize")

#
# Sanity check for casted data: each technique must have it's own column and this column
# must not contain any NA values.
#
sanityCheck <- function(col, t) {
    if(is.null(col)) {
        stop(paste("ERROR: Technique doesn't exist in data frame:", t));
    }
    if(any(is.na(col))) {
        stop(paste("ERROR: Missing data for technique:", t));
    }
}

# Generate a table for the top-n FL techniques:
# for each tuple (scoring scheme x scoring metric x agg_function)
get_top_n <- function(data_long, n, prefix="") {
    for (scheme in scoring_schemes) {
        for (agg in agg_functions) {
            for (val in scoring_metrics) {
                top_n <- getTopN(data_long, n, scheme, val, agg)
                sink(paste(out_dir,
                           paste(prefix, "bestBy",
                                 initialCap(val),
                                 initialCap(agg),
                                 initialCap(scheme),
                                 ".tex",
                                 sep=""),
                           sep="/"))
                printTechniqueTable(top_n, val)
                sink()

                # Sort all FL techniques and determine the best technique for each family
                all <- getTopN(data_long, length(getAllTechniques(data_long)), scheme, val, agg)
                best_by_family <- all[match(unique(all$Family), all$Family),]
                best_by_family <- best_by_family[order(best_by_family[val]),]

                sink(paste(out_dir,
                           paste(prefix, "bestInFamilyBy",
                                 initialCap(val),
                                 initialCap(agg),
                                 initialCap(scheme),
                                 ".tex",
                                 sep=""),
                           sep="/"))

                printTechniqueTable(best_by_family, val)
                sink()
            }
        }
    }
}

# Generate LaTex tables for the comparison of the best FL technique
# with all other FL techniques, considering different scoring schemes,
# aggregation functions, and score vs. rank values.
compare_best <- function(data_long, prefix="") {
    # Pre-defined significance level
    ALPHA <- 0.05

    all_techniques <- getAllTechniques(data_long)
    test_suite <- "developer"

    # Pair-wise compare the best technique with all other techniques for all scoring
    # schemes, scores vs. ranks, and mean vs. median
    for (scoring_scheme in scoring_schemes) {
        sink(paste(out_dir,
             paste(prefix, "compareBest",
                    initialCap(scoring_scheme),
                    ".tex",
                    sep=""),
             sep="/"))

        # scores vs. ranks
        for (agg_column in scoring_metrics) {
            # Cast the data into wide format (i.e., one column of scores/ranks per FL technique)
            data_wide <- castAll(data_long, agg_column)

            # mean vs. median
            for (agg_function in agg_functions) {
                best_technique_df <- getTopN(data_long, 1, scoring_scheme, agg_column, agg_function)
                best_technique_str <- getAllTechniques(best_technique_df)

                # Select column for the best technique
                best_col <- data_wide[[paste(test_suite, scoring_scheme, best_technique_str, sep="_")]]
                sanityCheck(best_col, best_technique_str)

                num_all_flts       <- 0
                best_is_better_sig <- 0
                best_is_worse_sig  <- 0
                best_is_insig      <- 0
                all_a12s           <- vector()
                all_ds             <- vector()

                # Iterate over all techniques
                for(t in all_techniques) {
                    if(t!=best_technique_str) {
                        num_all_flts <- num_all_flts + 1
                        other_col <- data_wide[[paste(test_suite, scoring_scheme, t, sep="_")]]
                        sanityCheck(other_col, t)
                        # Set p value to 1 and effect size to 0.5 if the techniques'
                        # scores (or ranks) are all identical.
                        if(isTRUE(all.equal(best_col, other_col))) {
                            p   <- 1
                            a12 <- 0.5
                            d   <- 0
                            best_is_insig <- best_is_insig + 1
                        } else {
                            # Test whether the EXAM scores (or their ranks) of the
                            # best technique are significantly lower than those of
                            # the other technique
                            t_test <- t.test(best_col, other_col, paired=TRUE)
                            p <- t_test$p.value
                            if(p >= ALPHA) {
                                best_is_insig <- best_is_insig + 1
                            } else if(t_test$estimate < 0) {
                                best_is_better_sig <- best_is_better_sig + 1
                            } else {
                                best_is_worse_sig <- best_is_worse_sig + 1
                            }
                            # Invert effect size such that a larger effect size is better
                            a12 <- 1 - (A12(best_col, other_col))
                            d <- cohen.d(best_col, other_col, paired=TRUE)$estimate

                        }
                        all_a12s[num_all_flts] <- a12
                        all_ds[num_all_flts]   <- d
                    }
                }
                cat(
                    # Make each row depending on a bool macro, which allows showing/hiding
                    # that row without re-running the stats script.
                    paste("\\ifbool{show", initialCap(agg_function), initialCap(agg_column), "}", sep=""),
                    "{",
                    paste(initialCap(agg_function), paste("\\", initialCap(agg_column), sep="")), " & ",
                    getTechnique(best_technique_df), " & ",
                    paste(best_is_better_sig, "/", num_all_flts, sep=""), " & ",
                    #paste(best_is_worse_sig, "/", num_all_flts, sep=""), " & ",
                    paste(best_is_insig, "/", num_all_flts, sep=""), " & ",
                    mean(all_ds), " \\\\",
                    "}",
                    "{}", "\n", sep="")
            }
        }
        sink()
    }
}

# Read the data file into a data frame
data_long <- readCsv(data_file, getArtificial=FALSE)
# Compute best technique for all families
get_top_n(data_long, 25)
compare_best(data_long)

# Compute best technique for all sbfl and mbfl only
data_long <- data_long[Family=='sbfl'|Family=='mbfl']
get_top_n(data_long, 25, "sbfl_mbfl_")
compare_best(data_long, "sbfl_mbfl_")
