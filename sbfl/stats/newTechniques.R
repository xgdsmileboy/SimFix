# Script that plots the distributions of the exam score and absolute score of the new
# techniques. 
#
# usage: Rscript newTechniques.R <data_file> <out_dir>
#

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript newTechniques.R <data_file> <out_dir>")
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
df_mcbfl <- subset(df, !df$ScoringScheme=="mean" & 
           (df$FLT %in% c("Metallaxis", "DStar", "MCBFL", "MCBFL-hybrid-failover", "MCBFL-hybrid-avg", "MCBFL-hybrid-max")))
df_mcbfl$FLT <- factor(df_mcbfl$FLT, levels=c("MCBFL-hybrid-failover",
                                              "MCBFL", 
                                              "MCBFL-hybrid-max",
                                              "Metallaxis",
                                              "MCBFL-hybrid-avg",
                                              "DStar"))

# Compare the new MRSBFL techniques with the best sbfl, mbfl, and mcbfl techniques
df_mrsbfl <- subset(df, !df$ScoringScheme=="mean" & 
           (df$FLT %in% c("Metallaxis", "DStar", "MRSBFL-hybrid-avg", "MCBFL", "MCBFL-hybrid-avg")))
df_mrsbfl$FLT <- factor(df_mrsbfl$FLT, levels=c("MCBFL-hybrid-avg",
                                                "MCBFL",
                                                "MRSBFL-hybrid-avg",
                                                "Metallaxis",
                                                "DStar"))

##########################################################################################
# Generate plots
#
theme <- theme(axis.title=element_text(size=34),
      legend.text=element_text(size=28),
      legend.title=element_text(size=28),
      legend.position="top",
      legend.margin=unit(12, "pt"),
      legend.key = element_blank(),
      axis.text.y = element_text(size=30),
      axis.text.x = element_text(size=30),
      strip.text.x = element_text(size=34, face="bold"),
      strip.text.y = element_text(size=34, face="bold")) 

guides <- guides(
    col = guide_legend(title.vjust=1, override.aes = list(size = 5), direction="horizontal", ncol=2, byrow=TRUE),
    linetype = guide_legend(title.vjust=1, direction="horizontal", ncol=2, byrow=TRUE))

guides_mcbfl <- guides(
    col = guide_legend(title.vjust=1, override.aes = list(size = 5), direction="horizontal", ncol=2, byrow=TRUE),
    linetype = guide_legend(title.vjust=1, direction="horizontal", ncol=1, byrow=TRUE))

options(scipen=10000)

# Plot the distribution of exam scores (log scale) -- MCBFL techniques, all projects
pdf(file=paste(out_dir, "mcbfl_distributions_ratio.pdf", sep="/"), pointsize=20, family="serif", width=15, height=15)
ggplot(df_mcbfl, aes(x=ScoreWRTLoadedClasses, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="Family: ", values=c("dashed", "solid", "dotted")) +
theme_bw() +
facet_grid(Scheme~.) + labs(x="EXAM score (log scale)", y="Density", color="FL technique: ") +
theme + guides_mcbfl + scale_x_log10(breaks = c(0.0001,0.001,0.01,0.1,1))
dev.off()

# Plot the distribution of absolute scores (log scale) -- MCBFL techniques, all projects
pdf(file=paste(out_dir, "mcbfl_distributions_abs.pdf", sep="/"), pointsize=20, family="serif", width=15, height=15)
ggplot(df_mcbfl, aes(x=ScoreAbs, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="Family: ", values=c("dashed", "solid", "dotted")) +
theme_bw() +
facet_grid(Scheme~.) + labs(x="Absolute score (log scale)", y="Density", color="FL technique: ") +
theme + guides_mcbfl + scale_x_log10(breaks = c(1,10,100,1000,10000))
dev.off()

# Plot the distribution of exam scores (log scale) -- MRSBFL techniques, all projects
pdf(file=paste(out_dir, "mrsbfl_distributions_ratio.pdf", sep="/"), pointsize=20, family="serif", width=15, height=15)
ggplot(df_mrsbfl, aes(x=ScoreWRTLoadedClasses, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("dashed", "solid", "solid", "twodash")) +
theme_bw() +
facet_grid(Scheme~.) + labs(x="EXAM score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(0.0001,0.001,0.01,0.1,1))
dev.off()

# Plot the distribution of absolute scores (log scale) -- MRSBFL techniques, all projects
pdf(file=paste(out_dir, "mrsbfl_distributions_abs.pdf", sep="/"), pointsize=20, family="serif", width=15, height=15)
ggplot(df_mrsbfl, aes(x=ScoreAbs, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("dashed", "solid", "solid", "twodash")) +
theme_bw() +
facet_grid(Scheme~.) + labs(x="Absolute score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(1,10,100,1000,10000))
dev.off()
