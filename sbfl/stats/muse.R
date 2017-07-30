# Script that plots the distributions of the exam score and absolute score for each kill
# definition for MUSE.
#
# usage: Rscript muse.R <data_file> <out_dir>
#

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript muse.R <data_file> <out_dir>")
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

# Compare the new MCBFL techniques with the best sbfl and mbfl techniques
df_muse <- subset(df, !df$ScoringScheme=="mean" & df$Formula=="muse" & df$AggregationDefn=="avg" & TotalDefn=="elements")
df_muse$KillDefn <- factor(df_muse$KillDefn, levels=c("exact", "type+message+location", "type+message", "type", "all", "passfail"))
##########################################################################################
# Generate plots
#
theme <- theme(axis.title=element_text(size=34),
      legend.text=element_text(size=34),
      legend.title=element_text(size=34),
      legend.position="top",
      legend.margin=unit(12, "pt"),
      legend.key = element_blank(),
      axis.text.y = element_text(size=30),
      axis.text.x = element_text(size=30),
      strip.text.x = element_text(size=34, face="bold"),
      strip.text.y = element_text(size=34, face="bold")) 

guides <- guides(col = guide_legend(override.aes = list(size = 5), direction="horizontal", ncol=6, byrow=TRUE))

options(scipen=10000)

# Plot the distribution of exam scores (log scale) -- MRSBFL techniques, all projects
pdf(file=paste(out_dir, "muse_distributions_ratio.pdf", sep="/"), pointsize=20, family="serif", width=30, height=20)
ggplot(df_muse, aes(x=ScoreWRTLoadedClasses, color=KillDefn)) + geom_line(stat="density", size=1.5) +
theme_bw() +
facet_grid(Scheme~FaultType) + labs(x="EXAM score (log scale)", y="Density", color="Kill definition: ") +
theme + guides + scale_x_log10(breaks = c(0.0001,0.001,0.01,0.1,1))
dev.off()

# Plot the distribution of absolute scores (log scale) -- MRSBFL techniques, all projects
pdf(file=paste(out_dir, "muse_distributions_abs.pdf", sep="/"), pointsize=20, family="serif", width=30, height=20)
ggplot(df_muse, aes(x=ScoreAbs, color=KillDefn)) + geom_line(stat="density", size=1.5) +
theme_bw() +
facet_grid(Scheme~FaultType) + labs(x="Absolute score (log scale)", y="Density", color="Kill definition: ") +
theme + guides + scale_x_log10(breaks = c(1,10,100,1000,10000))
dev.off()
