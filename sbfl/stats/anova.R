# Script that generates LaTex tables for the regression analysis and anova.
#
# usage: Rscript anova.R <data_file> <out_dir>
#

# Collection of helper functions
source("util.R")

# Read file name of the data file and the output directory
args <- commandArgs(trailingOnly = TRUE)
data_file <- args[1]
out_dir <- args[2]

# Check number of arguments
if (length(args)!=2) {
    stop("usage: Rscript anova.R <data_file> <out_dir>")
}

data <- readCsv(data_file)
both <- subset(data, (Family=="sbfl"|Family=="mbfl") & ScoringScheme!="mean")
both$Family <- droplevels(both$Family)
both$Comb <- as.factor(paste(both$Family,both$KillDefn,both$AggregationDefn))

sbfl <- subset(both, Family=="sbfl")
mbfl <- subset(both, Family=="mbfl")

isSigP <- function(p) {
    if(is.na(p)) {
        return("NA")
    } else if(p<0.01) {
        return(paste("\\sigStrong{", "<", 0.01, "}", sep=""))
    } else if(p<0.05) {
        return(paste("\\sigModerate{", "<", 0.05, "}", sep=""))
    } else {
        return(sprintf("\\insig{%.2f}", p))
    }
}

doAnova <- function(family, df, formula, factors) {
    # Fit regression model and determine R^2
    model <- lm(formula, data=df)
    r2  <- signif(summary(model)$r.squared, digits=2)

    # Perfrom anova and a post-hoc test for the factor "Formula"
    aov <- aov(formula, data=df)
    tukey_formula <- TukeyHSD(aov, "FormulaMacro")$FormulaMacro
    
    # Obtain the anova table
    anova <- anova(aov)

    # Write the anova table    
    sink(paste(out_dir, "anova_R2.tex", sep="/"), append=TRUE)
    cat(paste("\\def\\", gsub("_", "", family), "Rsqr", sep=""), "{", r2, "}", "\n")
    sink()
    sink(paste(out_dir, paste(family, "anova.tex", sep="_"), sep="/"))
    printAnovaTable(anova, factors)
    sink()
 
    # Write the results of the post hoc test
    sink(paste(out_dir, paste(family, "tukey_formula.tex", sep="_"), sep="/"))
    printTukeyResultsTable(tukey_formula)
    sink()

    # Return data frame with the results of the Tukey post-hoc test
    tuk_df <- data.frame(tukey_formula)
    # Convert data frames -> add column for row names, which indicate the compared pair
    tuk_df <- setDT(tuk_df, keep.rownames = TRUE)[]
    colnames(tuk_df) <- c("Pair", "Difference", "Lower", "Upper", "p")

    # Pre-defined significance level
    ALPHA <- 0.05
    prettyP <- function(p) {isSigP(p)}
    tuk_df$p  <- lapply(tuk_df$p, prettyP)

    return(tuk_df)
}

# Write file that defines the macros for the R^2 values
sink(paste(out_dir, "anova_R2.tex", sep="/"), append=FALSE)

facAll  <- c("Defect", "\\DebuggingScenario", "Family", "Formula", "Total definition", "Residuals")
formAll <- ScoreWRTLoadedClasses ~ ID + ScoringScheme + Comb + FormulaMacro + TotalDefn
tuk_both<- doAnova("sbfl_mbfl", both, formAll, facAll)

facSbfl  <- c("Defect", "\\DebuggingScenario", "Formula", "Total definition", "Residuals")
formSbfl <- ScoreWRTLoadedClasses ~ ID + ScoringScheme + FormulaMacro + TotalDefn
tuk_sbfl <- doAnova("sbfl", sbfl, formSbfl, facSbfl)

facMbfl  <- c("Defect", "\\DebuggingScenario", "Formula", "Total definition", "Interaction definition", "Aggregation definition", "Residuals")
formMbfl <- ScoreWRTLoadedClasses ~ ID + ScoringScheme + FormulaMacro + TotalDefn + KillDefn + AggregationDefn
tuk_mbfl <- doAnova("mbfl", mbfl, formMbfl, facMbfl)

sink(paste(out_dir, "tukey_formula_all.tex", sep="/"))
row <- paste(paste(gsub("-", " & ", tuk_both$Pair), tuk_sbfl$p, tuk_mbfl$p, tuk_both$p, sep=" & "), "\\\\ \n")
cat(row)
sink()
