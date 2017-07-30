# Script that replicates prior studies by comparing existing techniques on artificial and
# real faults. For each pair of techniques, the script performs a paired t test and
# computes the Cohen's d effect size.
#
# usage: Rscript replication.R <data_file> <out_dir>
#

source("util.R")
library(ggplot2)
library(extrafont)

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=2) {
    stop("usage: Rscript replication.R <data_file> <out_dir>")
}
data_file <- args[1]
out_dir <- args[2]

# Read data file and add two columns
df <- readCsv(data_file, getReal=TRUE, getArtificial=TRUE)
df$Real <- getReal(df)
df$FaultType <- ifelse(df$Real, "Real faults", "Artificial faults")
df$FLT <- prettifyTechniqueName(df$Technique)
df$Type <- getType(df$Technique)

# Pair artificial and real faults
df$RealId <- paste(df$Project, df$RealBugId, sep="-")
df <- df[df$RealId %in% intersect(unique(df[df$Real,]$RealId), unique(df[!df$Real,]$RealId)),]

metric <- "ScoreWRTLoadedClasses"

################################################################################
# Generate plots
#
theme <- theme(axis.title=element_text(size=34),
      legend.text=element_text(size=34),
      legend.title=element_text(size=34),
      legend.position="top",
      legend.box="horizontal",
      legend.margin=unit(12, "pt"),
      axis.text.y = element_text(size=30),
      axis.text.x = element_text(size=30),
      strip.text.x = element_text(size=34, face="bold"),
      strip.text.y = element_text(size=34, face="bold"))

guides <- guides(col = guide_legend(override.aes = list(size = 5), direction="horizontal", ncol=8, byrow=TRUE))

options(scipen=10000)

# Plot the distribution of exam scores (log scale) -- all projects
pdfname <- paste(out_dir, "distributions_ratio.pdf", sep="/")
pdf(file=pdfname, pointsize=20, family="serif", width=30, height=7)
ggplot(df, aes(x=ScoreWRTLoadedClasses, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("twodash", "solid")) +
theme_bw() +
facet_grid(~FaultType) + labs(x="EXAM score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(1,0.1,0.01,0.001,0.0001))
dev.off()
embed_fonts(pdfname, options="-dSubsetFonts=true -dEmbedAllFonts=true -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress -dMaxSubsetPct=100")


# Plot the distribution of exam scores (log scale) -- per project
pdfname <- paste(out_dir, "distributions_ratio_per_project.pdf", sep="/")
pdf(file=pdfname, pointsize=20, family="serif", width=30, height=30)
ggplot(df, aes(x=ScoreWRTLoadedClasses, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("twodash", "solid")) +
theme_bw() +
facet_grid(Project ~FaultType) + labs(x="EXAM score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(1,0.1,0.01,0.001,0.0001))
dev.off()
embed_fonts(pdfname, options="-dSubsetFonts=true -dEmbedAllFonts=true -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress -dMaxSubsetPct=100")

# Plot the distribution of absolute scores (log scale) -- all projects
pdfname <- paste(out_dir, "distributions_abs.pdf", sep="/")
pdf(file=pdfname, pointsize=20, family="serif", width=30, height=7)
ggplot(df, aes(x=ScoreAbs, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("twodash", "solid")) +
theme_bw() +
facet_grid(~FaultType) + labs(x="Absolute score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(1,10,100,1000,10000))
dev.off()
embed_fonts(pdfname, options="-dSubsetFonts=true -dEmbedAllFonts=true -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress -dMaxSubsetPct=100")

# Plot the distribution of absolute scores (log scale) -- per project
pdfname <- paste(out_dir, "distributions_abs_per_project.pdf", sep="/")
pdf(file=pdfname, pointsize=20, family="serif", width=30, height=30)
ggplot(df, aes(x=ScoreAbs, color=FLT, linetype=Type)) + geom_line(stat="density", size=1.5) +
scale_linetype_manual(name="     Family: ", values=c("twodash", "solid")) +
theme_bw() +
facet_grid(Project~FaultType) + labs(x="Absolute score (log scale)", y="Density", color="FL technique: ") +
theme + guides + scale_x_log10(breaks = c(1,10,100,1000,10000))
dev.off()
embed_fonts(pdfname, options="-dSubsetFonts=true -dEmbedAllFonts=true -dCompatibilityLevel=1.4 -dPDFSETTINGS=/prepress -dMaxSubsetPct=100")

################################################################################
# Generate table
#
# Cast all scoring metrics to wide format at once
wide <- dcast(setDT(df), "ID + Real ~ Technique", value.var=scoring_metrics)

#
# Comparisons made by prior studies
#
comparisons <- data.frame(
    Better=   c("ochiai",    "barinel", "barinel",   "opt2",   "opt2",     "dstar2", "dstar2",    "ochiai",  "jaccard",   "barinel", "opt2",    "metallaxis", "muse", "muse",      "muse"),
    Worse=    c("tarantula", "ochiai",  "tarantula", "ochiai", "tarantula","ochiai", "tarantula", "jaccard", "tarantula", "jaccard", "jaccard", "ochiai",     "opt2", "tarantula", "jaccard"),
    Citations=c("\\cite{naish2011model,LeTL2013,WongDGL2014,Xuan2014,Le2015}",
                "\\cite{abreu2009spectrum}",
                "\\cite{abreu2009spectrum}",
                "\\cite{naish2011model}",
                "\\cite{naish2011model,MoonKKY2014}",
                "\\cite{WongDGL2014,Le2015}",
                "\\cite{WongDGL2014,Ju2014,Le2015}",
                "\\cite{PapadakisLT2015}",
                "\\cite{MoonKKY2014}",
                "\\cite{MoonKKY2014}",
                "\\cite{abreu2007accuracy,abreu2009practical,abreu2009spectrum,naish2011model,MoonKKY2014,Xuan2014}",
                "\\cite{abreu2007accuracy,abreu2009practical,abreu2009spectrum,naish2011model,Xuan2014}",
                "\\cite{abreu2009spectrum}",
                "\\cite{naish2011model,MoonKKY2014}",
                "\\cite{MoonKKY2014}"))

# Output file for the generated table
sink(paste(out_dir, "table_replication.tex", sep="/"))

# Perform all comparisons
n_stat_sig_real <- 0
n_stat_sig_artf <- 0
n_both_sig_real <- 0
n_both_sig_artf <- 0
n_stat_sig_agreements_real <- 0
n_pract_sig_agreements_real <- 0
n_stat_sig_agreements_artf <- 0
n_pract_sig_agreements_artf <- 0
n_stat_sig_disagreements_real <- 0
n_pract_sig_disagreements_real <- 0
n_stat_sig_disagreements_artf <- 0
n_pract_sig_disagreements_artf <- 0
n_both_sig_agreements_real <- 0
n_both_sig_agreements_artf <- 0
n_both_sig_disagreements_real <- 0
n_both_sig_disagreements_artf <- 0
for (i in 1:length(comparisons$Better)) {
    prior_winner = comparisons$Better[i]
    prior_loser  = comparisons$Worse[i]
    citation     = comparisons$Citations[i]

    # Visually separate sbfl and mbfl comparisons
    if (prior_winner == "metallaxis") {
        cat("\\midrule \n")
    }

    # Print citations and the compared techniques
    cat(sprintf("%12s > %12s \\citation{%s}",
            prettifyTechniqueName(prior_winner),
            prettifyTechniqueName(prior_loser),
            citation))

    # Perform the same test for artificial and real faults
    for (real in c(FALSE, TRUE)) {
        mask       <- if (real) wide$Real else !wide$Real
        flt1_score <- paste(metric, prior_winner, sep="_")
        flt2_score <- paste(metric, prior_loser, sep="_")

        # Perform a paired, two-tailed t-test
        t_test <- t.test((wide[mask][[flt1_score]]), (wide[mask][[flt2_score]]), paired=TRUE)
        p      <- t_test$p.value
        # Determine the confidence interval (lower, upper)
        est    <- t_test$estimate
        lwr    <- t_test$conf.int[1]
        upr    <- t_test$conf.int[2]
        # Compute Cohen's d effect size
        d      <- cohen.d((wide[mask][[flt1_score]]), (wide[mask][[flt2_score]]), paired=TRUE)$estimate

        # Update the replication macros
        agree <- (est < 0)
        stat_sig <- (p < 0.05)
        pract_sig <- (abs(d) >= 0.2)
        if (pract_sig) {
          if (agree) {
            if (real) {n_pract_sig_agreements_real <- n_pract_sig_agreements_real+1;}
            else {n_pract_sig_agreements_artf <- n_pract_sig_agreements_artf+1;}
          } else {
            if (real) {n_pract_sig_disagreements_real <- n_pract_sig_disagreements_real+1;}
            else {n_pract_sig_disagreements_artf <- n_pract_sig_disagreements_artf+1;}
          }
        }
        if (stat_sig) {
          if (real) {n_stat_sig_real <- n_stat_sig_real+1;}
          else {n_stat_sig_artf <- n_stat_sig_artf+1;}
          if (agree) {
            if (real) {n_stat_sig_agreements_real <- n_stat_sig_agreements_real+1;}
            else {n_stat_sig_agreements_artf <- n_stat_sig_agreements_artf+1;}
          } else {
            if (real) {n_stat_sig_disagreements_real <- n_stat_sig_disagreements_real+1;}
            else {n_stat_sig_disagreements_artf <- n_stat_sig_disagreements_artf+1;}
          }
        }
        if (pract_sig && stat_sig) {
          if (real) {n_both_sig_real <- n_both_sig_real+1;}
          else {n_both_sig_artf <- n_both_sig_artf+1;}
          if (agree) {
            if (real) {n_both_sig_agreements_real <- n_both_sig_agreements_real+1;}
            else {n_both_sig_agreements_artf <- n_both_sig_agreements_artf+1;}
          } else {
            if (real) {n_both_sig_disagreements_real <- n_both_sig_disagreements_real+1;}
            else {n_both_sig_disagreements_artf <- n_both_sig_disagreements_artf+1;}
          }
        }

        # Formatted text for: do we agree, based on p value and effect size?
        sig_text    <- significanceText(p, est)
        effect_text <- dText(round(d, 2))

        # Format the output
        cat(sprintf("& %3s & %s & %s", sig_text, effect_text, typesetCI(lwr, upr)))

        # Determine simple counts: how often is one techqniue better than the other?
        better <- sum(wide[mask][[flt1_score]] < wide[mask][[flt2_score]])
        worse  <- sum(wide[mask][[flt1_score]] > wide[mask][[flt2_score]])
        equal  <- sum(wide[mask][[flt1_score]] == wide[mask][[flt2_score]])
        #a12    <- A12((wide[mask][[flt1_score]]), (wide[mask][[flt2_score]]))
        #a12    <- a12Text(round(a12, 2))

        # Format the output
        cat(sprintf("& (%d--%d--%d)", better, equal, worse))

        # Just to visually inspect the distribution of the exam score differences
        #plot(density(((wide[mask][[flt1_score]]) - (wide[mask][[flt2_score]]))),
        #     main=sprintf("ScoreRatio difference: %s vs. %s (%s)", prior_winner, prior_loser, if (real) "real faults" else "artificial faults"))
    }
    cat("\\\\ \n")
}
sink()

sink(paste(out_dir, "macros_replication.tex", sep="/"))
cat("\\def\\nPriorComparisons{", nrow(comparisons), "\\xspace}\n", sep="")

for (real in c(TRUE, FALSE)) {
  fault_type <- ifelse(real, "Real", "Artificial")
  cat("\\def\\fractionOfPriorComparisonsWithStatSigOn", fault_type, "Faults{",
    round(100*ifelse(real, n_stat_sig_real, n_stat_sig_artf)/nrow(comparisons)),
    "\\%\\xspace}\n", sep="");
  cat("\\def\\fractionOfPriorComparisonsWithoutStatSigOn", fault_type, "Faults{",
    100-round(100*ifelse(real, n_stat_sig_real, n_stat_sig_artf)/nrow(comparisons)),
    "\\%\\xspace}\n", sep="");
    cat("\\def\\fractionOfPriorComparisonsWithBothSigOn", fault_type, "Faults{",
      round(100*ifelse(real, n_both_sig_real, n_both_sig_artf)/nrow(comparisons)),
      "\\%\\xspace}\n", sep="");
    cat("\\def\\fractionOfPriorComparisonsWithoutBothSigOn", fault_type, "Faults{",
      100-round(100*ifelse(real, n_both_sig_real, n_both_sig_artf)/nrow(comparisons)),
      "\\%\\xspace}\n", sep="");
  for (sig_type in c("Stat", "Pract", "Both")) {
    for (agree in c(TRUE, FALSE)) {
      noun <- ifelse(agree, "Agreement", "Disagreement")
      value <- ifelse( real&&(sig_type=="Stat" )&& agree, n_stat_sig_agreements_real,
               ifelse( real&&(sig_type=="Stat" )&&!agree, n_stat_sig_disagreements_real,
               ifelse( real&&(sig_type=="Pract")&& agree, n_pract_sig_agreements_real,
               ifelse( real&&(sig_type=="Pract")&&!agree, n_pract_sig_disagreements_real,
               ifelse( real&&(sig_type=="Both" )&& agree, n_both_sig_agreements_real,
               ifelse( real&&(sig_type=="Both" )&&!agree, n_both_sig_disagreements_real,
               ifelse(!real&&(sig_type=="Stat" )&& agree, n_stat_sig_agreements_artf,
               ifelse(!real&&(sig_type=="Stat" )&&!agree, n_stat_sig_disagreements_artf,
               ifelse(!real&&(sig_type=="Pract")&& agree, n_pract_sig_agreements_artf,
               ifelse(!real&&(sig_type=="Pract")&&!agree, n_pract_sig_disagreements_artf,
               ifelse(!real&&(sig_type=="Both" )&& agree, n_both_sig_agreements_artf,
               ifelse(!real&&(sig_type=="Both" )&&!agree, n_both_sig_disagreements_artf
               ))))))))))))

      cat("\\def\\nPriorComparisonsWith", sig_type, "Sig", noun, "On", fault_type, "Faults{", value, "\\xspace}\n", sep="");
      cat("\\def\\fractionOfPriorComparisonsWith", sig_type, "Sig", noun, "On", fault_type, "Faults{", round(100*value/nrow(comparisons)), "\\%\\xspace}\n", sep="");
      cat("\\def\\fractionOfPriorComparisonsWithout", sig_type, "Sig", noun, "On", fault_type, "Faults{", 100-round(100*value/nrow(comparisons)), "\\%\\xspace}\n", sep="");
      cat("\\def\\intensifiedNPriorComparisonsWith", sig_type, "Sig", noun, "On", fault_type, "Faults{",
        ifelse(value>0,
               paste('only', toString(value)),
               "\\emph{zero}"),
        "\\xspace}\n", sep="")
    }
  }
}
sink()
