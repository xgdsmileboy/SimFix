Tools for analyzing coverage/mutation reports.

* `java-parser`: a tool for identifying which lines in a Java program belong to the same statements. Used to translate information about which *line* something affects (e.g., a mutant or a bugfix patch) into information about which *statement* it affects.
* `muse-optimization-comparison`: Because MUSE is extremely expensive to compute, our experiments use an optimization.  That optimization is sometimes unsound.  This directory contains tools to determine whether the optimization produced an incorrect result (in which case the killmap needs to be regenerated without the optimization) or the optimization produced a killmap that fairly evaluates MUSE.
* `pipeline-scripts`: scripts for running fault localization techniques on the coverage/mutation data.
