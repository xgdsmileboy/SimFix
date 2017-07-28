require(ggplot2)

args <- commandArgs(trailingOnly = TRUE)
data_file <- args[1]
out_file <- args[2]

pdf(file=out_file, pointsize=20, family="serif", width=14, height=8)

df <- read.csv(header=TRUE, file=data_file)

# Group faults into real vs. artificial
df[df$Bug<1000,"FaultType"] <- "Real Faults"
df[df$Bug>1000,"FaultType"] <- "Artificial Faults"

# Add an additional FL technique column to simplify the labeling of the boxplot
df[df$Formula=="opt2","Technique"] <- "Op2 "
df[df$Formula=="barinel","Technique"] <- "Barinel "
df[df$Formula=="tarantula","Technique"] <- "Tarantula "
df[df$Formula=="dstar2","Technique"] <- "DStar "
df[df$Formula=="muse","Technique"] <- "MUSE "
df[df$Formula=="jaccard","Technique"] <- "Jaccard "
df[df$Family=="sbfl" & df$Formula=="ochiai","Technique"] <- "Ochiai "
df[df$Family=="mbfl" & df$Formula=="ochiai","Technique"] <- "Metallaxis "

ggplot(data = df, aes(x=FaultType, y=Score, fill=Technique)) +
    stat_boxplot(geom ='errorbar') + geom_boxplot() +
    ylab("") + xlab("") + scale_y_log10() + theme_bw() + labs(fill="") +
    guides(fill = guide_legend(nrow = 1)) +
    # Adjust font sizes and positioning of legend
    theme(axis.title=element_text(size=24),
          legend.text=element_text(size=24),
          legend.title=element_text(""),
          legend.position = "top",
          legend.direction = "horizontal",
          text = element_text(size=30))
dev.off()
