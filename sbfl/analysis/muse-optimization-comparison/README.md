Tools for identifying which faults a killmap-generating optimization needs to be disabled for.

While generating killmaps, we generally assume that any mutant not killed by any failing test is equally unsuspicious. For MUSE, this isn't true: a mutant killed by 0 failing tests and 1 passing test is less suspicious than one killed by 0 failing tests and 0 passing tests. So, if no failing test kills any mutant in any buggy statement, *with* our optimization all buggy statements will have 0 suspiciousness, just like most of the rest of the program; but *without* our optimization, this symmetry may be broken, affecting the buggy lines' rank; so our optimization is unsound.

Fortunately, if *any* failing test kills a mutant in *any* buggy statement, that statement will have nonzero suspiciousness, and so our optimization is still sound.

This directory has tools for identifying whether the optimization is unsound for a given killmap:

* `is-killmap-optimization-safe.sh` tells whether a given killmap is in danger of producing inaccurate scores for MUSE.
* `separate-faults-by-muse-mode.py` reads through all killmaps, and produces two files (`safe-muse-faults.csv` and `unsafe-muse-faults.csv`) identifying which killmaps are sound and which might not be.
* `score-for-comparison.sh` scores all killmaps with the optimization and without the optimization, and prints how many differ.
* `find-fixed-failing-tests.py` prints information correlating how well MUSE does on any given fault with whether the optimization is sound on that fault.
* `muse_optimization_comparison.py` provides utility functions.
