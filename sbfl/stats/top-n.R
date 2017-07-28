# Script that computes the ratio of defects that the best mbfl, sbfl, and hybrid
# techniques localize in the top-5, top-10, and top-200 of the suspiciousness ranking.
#
# usage: Rscript top-n.R <data_file> <out_dir>
#

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript top-n.R <data_file> <out_dir>")
}
data_file <- args[1]
out_dir <- args[2]

source("util.R")
library(ggplot2)

# Read data file and add two columns
df <- readCsv(data_file)
df$Real <- getReal(df)
df$FaultType <- ifelse(df$Real, "Real faults", "Artificial faults")

# Use the normalized EXAM score as scoring metric
metric <- "ScoreWRTLoadedClasses"
df$Log10ScoreRatio <- log10(df[[metric]])
df$Log10ScoreAbs   <- log10(df$ScoreAbs)

getScoringSchemes <- function(df) {
  return(ifelse(df$ScoringScheme=="first",  "Best-case",
         ifelse(df$ScoringScheme=="last",   "Worst-case",
         ifelse(df$ScoringScheme=="median", "Average-case", "N/A"))))

}

df$Scheme <- getScoringSchemes(df)
df$Scheme <- factor(df$Scheme, levels=c("Best-case", "Worst-case", "Average-case"))
df$Type   <- getType(df$Technique)

# New techniques:
#
# "MCBFL", "MCBFL-hybrid-failover", "MCBFL-hybrid-avg", "MCBFL-hybrid-max"
# "MRSBFL", "MRSBFL-hybrid-failover", "MRSBFL-hybrid-avg", "MRSBFL-hybrid-max", "MCBFL-hybrid-avg"

num_real_bugs <- length(unique(df[df$Real,ID]))
sink(paste(out_dir, "top-n.tex", sep="/"))
for (flt in c("MCBFL-hybrid-avg",
              "MRSBFL-hybrid-avg",
              "DStar",
              "Ochiai",
              "Jaccard",
              "Metallaxis",
              "Barinel",
              "Tarantula",
              "Op2",
              "MUSE")) {
    cat(flt)
    for (scheme in c("first", "last", "median")) {
        mask <- df$ScoringScheme==scheme & df$Real & df$FLT==flt
        cat(" & ")
        cat(round(nrow(df[mask & df$ScoreAbs<5.1,])/num_real_bugs*100, digits=0), "\\%", sep="")
        cat(" & ")
        cat(round(nrow(df[mask & df$ScoreAbs<10.1,])/num_real_bugs*100, digits=0), "\\%", sep="")
        cat(" & ")
        cat(round(nrow(df[mask & df$ScoreAbs<200.1,])/num_real_bugs*100, digits=0), "\\%", sep="")
    }
    cat("\\\\ \n")
}
sink()
