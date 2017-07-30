# Script that generates a LaTex table showing the ratios of passing and
# failing tests for real and artificial faults.
#
# usage: Rscript num_tests.R <num_tests_data_file> <artificial_vs_real_data_file> <out_dir>
#
source("util.R")

# Check number of arguments
args <- commandArgs(trailingOnly=T)
if (length(args) != 3) {
    stop("usage: Rscript num_tests.R <num_tests_data_file> <artificial_vs_real_data_file> <out_dir>")
}

# Read file name of the data file and the output directory
tests_data_file <- args[1]
scores_data_file <- args[2]
out_dir <- args[3]

df <- read.csv(tests_data_file, header=T)
df$Bug <- df$bug_id
df$Real <- getReal(df)
df$RealBugId <- as.integer(lapply(df$Bug, getRealBugId))
df$RealSubjectId <- paste(df$project_name, df$RealBugId, sep="-")

# Retain only paired faults
retain <- getPairedRealBugIds(readCsv(scores_data_file))
df <- df[df$RealSubjectId %in% retain,]

# Compute the ratios of failing and passing tests
df$ratio_failing_tests <- df$num_failing_tests/df$num_tests*100
df$ratio_passing_tests <- df$num_passing_tests/df$num_tests*100

# Aggregate artificial faults to account for the fact that some real faults
# have a significantly larger number of artificial faults associated with them.
df <- aggregate(cbind(num_tests,ratio_failing_tests,ratio_passing_tests) ~ RealSubjectId + project_name + RealBugId + Real + tool, data=df, FUN=median)

# Print the table of ratios of failing/passing tests
table = paste(out_dir, "table_num_tests.tex", sep="/")
unlink(table)
sink(table, append=TRUE, split=TRUE)
for (project in sort(unique(df$project_name))) {
    mask_p <- df$project_name == project

    cat(project, " & ", round(median(df[mask_p & df$Real,]$num_tests)), sep="")
    for (real in c(0, 1)) {
        mask_real   <- df$Real == real
        ratio_pass <- sprintf("%.1f", median(df$ratio_passing_tests[mask_p & mask_real]))
        ratio_fail <- sprintf("%.1f", median(df$ratio_failing_tests[mask_p & mask_real]))
        cat(" & ", ratio_pass, "\\% & ", ratio_fail, "\\%", sep="")
    }
    cat(" \\\\ \n")
}
sink()
