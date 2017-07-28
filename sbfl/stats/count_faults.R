# Count the total number of artificial faults and real faults, and the number of
# aggregated artificial faults and matched pairs.
#
# usage: Rscript count_faults.R <data_file>
#

source("util.R")
library(ggplot2)

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=1) {
    stop("usage: Rscript count_faults.R <data_file>")
}
data_file <- args[1]
 
# Read data file and add two columns
data <- readCsv(data_file, getReal=TRUE, getArtificial=TRUE)
data$Real <- getReal(data)
data$Id <- paste(data$Project, data$Bug, sep="-")
data$RealId <- paste(data$Project, data$RealBugId, sep="-")

cat("Real faults total: ")
cat(length(unique(data[data$Real,]$RealId)), "\n")

cat("Artificial faults total: ")
cat(length(unique(data[!data$Real,]$Id)), "\n")

cat("Artificial faults aggregated: ")
cat(length(unique(data[!data$Real,]$RealId)), "\n")

cat("Paired faults: ")
cat(length(intersect(unique(data[data$Real,]$RealId), unique(data[!data$Real,]$RealId))), "\n")

cat("Missing: ")
cat((setdiff(unique(data[data$Real,]$RealId), unique(data[!data$Real,]$RealId))), "\n")
