# Script that compares our new techniques with existing techniques on artificial and
# real faults. For each pair of techniques, the script performs a paired t test and
# computes the Cohen's d effect size.
#
# usage: Rscript compNewTechniques.R <data_file> <out_dir>
#

source("util.R")

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript compNewTechniques.R <data_file> <out_dir>")
}
data_file <- args[1]
out_dir <- args[2]

# Read data file and add two columns
df <- readCsv(data_file, getReal=TRUE, getArtificial=TRUE)
df$FLT <- prettifyTechniqueName(df$Technique)

#
# All comparisons
#
comparisons <- data.frame(
    Better=   c("susp-averaging", "susp-averaging","susp-averaging","susp-averaging","susp-averaging","susp-averaging","susp-averaging","susp-averaging","susp-averaging",
                "mrsbfl-susp-averaging", "mrsbfl-susp-averaging","mrsbfl-susp-averaging","mrsbfl-susp-averaging","mrsbfl-susp-averaging","mrsbfl-susp-averaging","mrsbfl-susp-averaging","mrsbfl-susp-averaging"),
    Worse=   c("barinel", "dstar2", "ochiai", "opt2", "tarantula", "metallaxis", "muse", "jaccard", "mrsbfl-susp-averaging",
               "barinel", "dstar2", "ochiai", "opt2", "tarantula", "metallaxis", "muse", "jaccard"))

metric <- "ScoreWRTLoadedClasses"

df <- df[df$Technique %in% comparisons$Better | df$Technique %in% comparisons$Worse,]

################################################################################
# Generate table
#
# Cast all scoring metrics to wide format at once
wide <- dcast(setDT(df), "ID ~ ScoringScheme + Technique", value.var=scoring_metrics)

# Output file for the generated table
sink(paste(out_dir, "compare_new_techniques.tex", sep="/"))

# Perform all comparisons
for (i in 1:length(comparisons$Better)) {
    prior_winner = comparisons$Better[i]
    prior_loser  = comparisons$Worse[i]

    # Visually separate comparisons with different families
    if (prior_loser == "metallaxis" | prior_loser == "mrsbfl-susp-averaging") {
        cat("\\midrule \n")
    }

    # Print citations and the compared techniques
    cat(sprintf("%s vs. %s ",
            prettifyTechniqueName(prior_winner),
            prettifyTechniqueName(prior_loser)))

    # Perform the same test for all debugging scenarios
    for (scheme in c("first", "last", "median")) {
        flt1_score <- paste(metric, scheme, prior_winner, sep="_")
        flt2_score <- paste(metric, scheme, prior_loser, sep="_")

        # Perform a paired, two-tailed t-test
        t_test <- t.test((wide[[flt1_score]]), (wide[[flt2_score]]), paired=TRUE)
        p      <- t_test$p.value
        # Determine the confidence interval (lower, upper)
        est    <- t_test$estimate
        lwr    <- t_test$conf.int[1]
        upr    <- t_test$conf.int[2]
        # Compute Cohen's d effect size
        d      <- cohen.d((wide[[flt1_score]]), (wide[[flt2_score]]), paired=TRUE)$estimate

        # Formatted text for: do we agree, based on p value and effect size?
        sig_text    <- significanceNumber(p)
        effect_text <- dText(round(d, 2))

        # Format the output
        cat(sprintf("& %s & %s", sig_text, effect_text))
    }
    cat("\\\\ \n")
    
    # Visually separate comparisons with different families
    if (prior_loser == "mrsbfl-susp-averaging") {
        cat("\\midrule \n")
        cat("\\midrule \n")
    }
}
sink()
