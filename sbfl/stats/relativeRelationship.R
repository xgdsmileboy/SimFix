# Script that computes the Spearman rho and associated p-value
# for (FLT ranks for real faults, FLT ranks for artificial faults)
#
# usage: Rscript relativeRelationship.R
#                    <data_file>
#                    <multiline_with_omission_data>
#                    <multiline_without_omission_data>
#                    <singleline_with_omission_data>
#                    <singleline_without_omission_data>
#                    <out_dir>
#

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
if (length(args)!=6) {
    stop("usage: Rscript relativeRelationship.R <data_file> <multiline_with_omission_data> <multiline_without_omission_data> <singleline_with_omission_data> <singleline_without_omission_data> <out_dir>")
}
out_dir         <- args[6]

source("util.R")

tournamentPointsMean <- function(wide, techniques, metric) {
  result <- rep(0, length(techniques))
  for (i in 1:(length(techniques)-1)) {
    for (j in (i+1):length(techniques)) {
      flt1_col <- paste(metric, techniques[i], sep="_")
      flt2_col <- paste(metric, techniques[j], sep="_")
      # No need to run the t-test if the samples are identical
      if (identical(wide[[flt1_col]], wide[[flt2_col]])) {
        p   <- 1;
        est <- 0;
      } else {
        t_test <- t.test(wide[[flt1_col]], wide[[flt2_col]], paired=TRUE)
        p      <- t_test$p.value
        est    <- t_test$estimate
      }
      # TODO: Check whether we need a correction for multiple comparisons here
      if (p < 0.05) {
        winner = if (est < 0) i else j
        result[winner] = result[winner]+1
      }
    }
  }
  return(result)
}

GENERATED_FILES = paste(out_dir,sep="")

generateScoreAndPointsTable <- function(name, techniques, scoresReal, scoresArtf, pointsReal, pointsArtf, suffix = "", digits = 4) {
    if(nchar(suffix) > 0) {
        name = paste(name, suffix, sep="_")
    }
    print(name)
    TABLE = paste(GENERATED_FILES, "/table_", name, ".tex", sep="")
    unlink(TABLE)
    sink(TABLE, append=TRUE, split=TRUE)
    cat("\\begin{tabular}{lcc@{\\hspace{2em}}lcc}\\toprule", "\n")
    cat("\\multicolumn{3}{c}{\\textbf{Artificial Faults}} & \\multicolumn{3}{c}{\\textbf{Real Faults}} \\\\", "\n")
    cat("Technique & \\exam & \\# Worse & Technique & \\exam & \\# Worse \\\\ \n")
    cat("\\midrule","\n")
    realSorted = sort.int(scoresReal, index.return=TRUE, decreasing=FALSE)$ix
    artfSorted = sort.int(scoresArtf, index.return=TRUE, decreasing=FALSE)$ix
    for (i in 1:length(techniques)) {
        indexReal = realSorted[i]
        indexArtf = artfSorted[i]
        cat(
            prettifyTechniqueName(techniques[indexArtf]), " & ",
            formatC(scoresArtf[indexArtf], digits=digits, format="f"), " & ",
            formatC(pointsArtf[indexArtf], digits=digits, format="d"), " & ",
            prettifyTechniqueName(techniques[indexReal]), " & ",
            formatC(scoresReal[indexReal], digits=digits, format="f"), " & ",
            formatC(pointsReal[indexReal], digits=digits, format="d"),
            "\\\\ \n")
    }
    cat("\\bottomrule","\n")
    cat("\\end{tabular}","\n")
    sink()
}

generateTable <- function(name, header, techniques, valuesReal, valuesArtf, suffix = "", decreasing = FALSE, digits = 4, integer=FALSE) {
    if(nchar(suffix) > 0) {
        name = paste(name, suffix, sep="_")
    }
    print(name)
    TABLE = paste(GENERATED_FILES, "/table_", name, ".tex", sep="")
    unlink(TABLE)
    sink(TABLE, append=TRUE, split=TRUE)
    cat("\\begin{tabular}{lc@{\\hspace{2em}}lc}\\toprule", "\n")
    cat("\\multicolumn{2}{c}{\\textbf{Artificial Faults}} & \\multicolumn{2}{c}{\\textbf{Real Faults}} \\\\", "\n")
    cat("Technique & ", header, " & Technique & ", header, "\\\\ \n")
    cat("\\midrule","\n")
    realSorted = sort.int(valuesReal, index.return=TRUE, decreasing=decreasing)$ix
    artfSorted = sort.int(valuesArtf, index.return=TRUE, decreasing=decreasing)$ix
    format_char = ifelse(integer, "d", "f")
    for (i in 1:length(techniques)) {
        indexReal = realSorted[i]
        indexArtf = artfSorted[i]
        cat(
            prettifyTechniqueName(techniques[indexArtf]), " & ",
            formatC(valuesArtf[indexArtf], digits=digits, format=format_char), " & ",
            prettifyTechniqueName(techniques[indexReal]), " & ",
            formatC(valuesReal[indexReal], digits=digits, format=format_char),
            "\\\\ \n")
    }
    cat("\\bottomrule","\n")
    cat("\\end{tabular}","\n")
    sink()

    REAL_ROW = paste(GENERATED_FILES, "/table_", name, "_RealRow.tex", sep="")
    unlink(REAL_ROW)
    sink(REAL_ROW, append=TRUE, split=TRUE)
    cat(sapply(techniques[realSorted], prettifyTechniqueName), sep=" & ")
    sink()
    ARTF_ROW = paste(GENERATED_FILES, "/table_", name, "_ArtfRow.tex", sep="")
    unlink(ARTF_ROW)
    sink(ARTF_ROW, append=TRUE, split=TRUE)
    cat(sapply(techniques[artfSorted], prettifyTechniqueName), sep=" & ")
    sink()
}

data_names = c("", "multiline_with_omission", "multiline_without_omission", "single_line_with_omission", "single_line_without_omission")
macro_names = c(
    "tournamentRealVsArtificialSpearmanPValue",
    "tournamentRealVsArtificialSpearmanPValueMultilineWithOmission",
    "tournamentRealVsArtificialSpearmanPValueMultilineWithoutOmission",
    "tournamentRealVsArtificialSpearmanPValueSingleLineWithOmission",
    "tournamentRealVsArtificialSpearmanPValueSingleLineWithoutOmission")


MACRO_FILE=paste(GENERATED_FILES, "/macros_relative.tex", sep="")
unlink(MACRO_FILE)
sink(MACRO_FILE)
cat("%% This file was automatically generated by relativeRelationship.R.\n")
sink()

for(data_index in 1:5) {
    data_file = args[data_index]
    data_name = data_names[data_index]
    df <- readCsv(data_file)
    df$Real <- getReal(df)
    df$Technique <- getTechniques(df)
    # Cast data to wide format
    wide <- dcast(setDT(df), "ID + Real ~ Technique", value.var=scoring_metrics)

    real_points_mean = tournamentPointsMean(wide[wide$Real,], techniques, "ScoreWRTLoadedClasses")
    artificial_points_mean = tournamentPointsMean(wide[!wide$Real,], techniques, "ScoreWRTLoadedClasses")
    spearman_mean <- cor.test(real_points_mean, artificial_points_mean, method="spearman")

    real_points_rank = tournamentPointsMean(wide[wide$Real,], techniques, "RANK")
    artificial_points_rank = tournamentPointsMean(wide[!wide$Real,], techniques, "RANK")
    spearman_rank <- cor.test(real_points_rank, artificial_points_rank, method="spearman")

    sink(MACRO_FILE, append=TRUE, split=TRUE)
    cat(sprintf("\\def\\%sMean{%.2f\\xspace}\n", macro_names[data_index], spearman_mean$p.value))
    cat(sprintf("\\newcommand\\asserting%sMeanIsLarge[1]{%s}\n",
                macro_names[data_index],
                ifelse(spearman_mean$p.value<0.05, "\\todo{FALSE: #1}", "#1")))
    cat(sprintf("\\def\\%sMedian{%.2f\\xspace}\n", macro_names[data_index], spearman_rank$p.value))
    sink()

    cat("\n")
    cat("Award each FLT 1 point for every other FLT it does significantly\n")
    cat("better than on real faults. Count #points for each technique.\n")
    cat("Do this again on artificial faults.\n")
    cat("\n")
    cat("H0: (#points on real faults)\n")
    cat("        is independent of\n")
    cat("    (#points on artificial faults)\n")
    cat("H1: they are correlated (either positively or negatively)\n")
    cat("\n")
    cat(sprintf("P-value: %f\n", spearman_mean$p.value))
    cat(sprintf("Rho:     %f\n", spearman_mean$estimate))

    cat("\nSummary of data:\n")
    technique_summaries <- data.frame(
        Technique=techniques,
        RealPoints=real_points_mean,
        ArtfPoints=artificial_points_mean,
        RealMean=rep(0, length(techniques)),
        ArtfMean=rep(0, length(techniques)),
        RealRankMean=rep(0, length(techniques)),
        ArtfRankMean=rep(0, length(techniques)))

    for (i in 1:length(techniques)) {
        real <- df[df$Real & (df$Technique==techniques[i]),]
        artf <- df[(!df$Real) & (df$Technique==techniques[i]),]
        technique_summaries$RealMean[i] = mean(real$ScoreWRTLoadedClasses)
        technique_summaries$ArtfMean[i] = mean(artf$ScoreWRTLoadedClasses)
        technique_summaries$RealRankMean[i] = mean(real$RANK)
        technique_summaries$ArtfRankMean[i] = mean(artf$RANK)
    }

    print(technique_summaries)

    generateTable("TournamentScore", "\\# Worse",  techniques, real_points_mean, artificial_points_mean, suffix = data_name, decreasing = TRUE, integer = TRUE)
    generateTable("TournamentRank", "\\# Worse",  techniques, real_points_rank, artificial_points_rank, suffix = data_name, decreasing = TRUE, integer = TRUE)
    generateTable("ScoreMean", "\\exam Score",  techniques, technique_summaries$RealMean, technique_summaries$ArtfMean, suffix = data_name, decreasing = FALSE)
    generateTable("RankMean", "\\fltRank",  techniques, technique_summaries$RealRankMean, technique_summaries$ArtfRankMean, digits=2, suffix = data_name, decreasing = FALSE)
    generateScoreAndPointsTable("ScoreAndPoints", techniques, technique_summaries$RealMean, technique_summaries$ArtfMean, real_points_mean, artificial_points_mean, suffix = data_name)
}
