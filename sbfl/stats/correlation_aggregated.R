# Script that computes the correlation for the EXAM score and FL rank between
# artificial and real faults, aggregating the results for artificial faults.
#
# usage: Rscript correlation_aggregated.R <data_file> <out_dir>
#

options(warn=1)

source("util.R")
library(ggplot2)

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript correlation_aggregated.R <data_file> <out_dir>")
}
data_file <- args[1]
out_dir <- args[2]
 
# Read data file and add two columns
data <- readCsv(data_file, getReal=TRUE, getArtificial=TRUE)
data$Real <- getReal(data)
data$FaultType <- ifelse(data$Real, "Real faults", "Artificial faults")
data$FLT <- prettifyTechniqueName(data$Technique)
data$Type <- getType(data$Technique)

# Aggregate the results for artificial faults
agg <- aggregate(cbind(ScoreWRTLoadedClasses,ScoreAbs,RANK) ~ Project + RealBugId + Real + Technique + FLT + Type + FaultType + ScoringScheme, data=data, FUN=mean)
agg$ID <- paste(agg$Project, agg$RealBugId)
data <- agg

flts <- c("ochiai", "tarantula", "barinel", "opt2", "dstar2", "jaccard", "metallaxis", "muse")
for (scheme in c("first", "median", "last")) {
    sink(paste(out_dir, paste("table_correlation_aggregated_", scheme, ".tex", sep=""), sep="/"))
    for (flt in flts) {
        cat(prettifyTechniqueName(flt))
        for (metric in scoring_metrics) {
            wide <- dcast(setDT(data), "ID ~ Real + Technique + ScoringScheme", value.var=scoring_metrics)
            wide <- wide[complete.cases(wide),]
        
            art  <- paste(metric, FALSE, flt, scheme, sep="_")
            real <- paste(metric, TRUE, flt, scheme, sep="_")

            for (m in c("pearson", "spearman")) {
                c <- cor(wide[[art]], wide[[real]], method=m)
                cat(" & ", sprintf("%.2f", round(c, 2)))
            }
        }
        cat("\\\\ \n")
    }
    sink()
}
