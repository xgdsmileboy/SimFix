# SimFix

* [I. Introduction of SimFix](#user-content-i-introduction)
* [II. Environment setup](#user-content-ii-environment)
* [III. Run SimFix Step-by-Step](#user-content-iii-how-to-run)
* [IV. Evaluation Result](#user-content-iv-evaluation-result)
* [V. Structure of the project](#user-content-v-structure-of-the-project)

## I. Introduction

*SimFix* is an automatic program repair technique, which leverages similar code snippets in the same project to generate patches. The following figure is the workflow of our approach.

![The workflow of this technique.\label{workflow}](./doc/figure/overview.png)

1. **Fault Localization** : obtain a ranking list of candidate buggy statements.
2. **Similar Code Identification** : identify the similarity between buggy code snippet and each candidate similar code snippet by leveraging three similarity metrics, according to which we obtain a list of candidate similar snippet with decending order of similarity value.
3. **Variable Mapping** : establish the mapping relationship between variables in buggy and similar code snippet by leveraging similarity metrics and then obtain a mapping table.
4. **Modification Extraction** : extract code modifications to buggy code snippet via AST (Abstract Syntax Tree) matching and differencing, during which the variable mapping table will be used.
5. **Patch Generation & Validation** : generate repair patches by applying extracted code modifications to the buggy code snippet with combining and ranking whose modifications, then using the test suite to validate the correctness for each candidate patch.

## II. Environment

* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* Download and configure [Defects4J](https://github.com/rjust/defects4j) running environment.
* Configure the following path.
  * DEFECTS4J_HOME="home_of_defects4j"



## III. How to run

*SimFix* was traditionally developed as an Eclipse Java project, you can simply import this project to your workspace and run it as a common Java program. The main class is **cofix.main.Main**, and for the running option please refer to the [Running Options](#user-content-step-2-running-options).

#### Before running

* `unzip` file `sbfl/data.zip` to `sbfl/data`  : used for fault localization

* using the command line provided by Defects4J to checkout a buggy version of benchmark for testing.

   `Example: defects4j checkout -p Chart -v 1b -w ${buggy_program_path}/chart/chart_1_buggy`

  **_NOTE_** : the path of the buggy version of benchmark have to be set as:

  `…/projectName/projectName_id_buggy`  => `Example: /home/user/chart/chart_1_buggy`

#### Step 1, Build The Project

Originally, *SimFix* was developed as an [Eclipse](http://www.eclipse.org/mars/) Java Project, you can simply **import** the project into your workspace and the class `cofix.main.Main` is the entry of the whole program.

#### Step 2, Running Options 

Our prototype of *SimFix* needs **three** input options for running.

* `--proj_home ` : the home of buggy program of benchmark. (`${buggy_program_path}` for the example)

* `--proj_name` : the project name of buggy program of benchmark. (`chart` for the example)

* `--bug_id` : the identifier of the buggy program. (`1` for the example)

  * the option of `--bugy_id` supports multiple formats:

    `single_id` : repair single bug, `e.g., 1`.

    `startId-endId` : repair a series of bugs with consecutive identifiers, `e.g., 1-3`.

    `single_id,single_id,single_id` : repair any bugs for the specific program, `e.g., 1,5,9`.

    `all` : repair all buggy versions of a specific project, `i.e., all`.

  ```shell
  Usage: --proj_home=${proj_home} --proj_name=${proj_name} --bug_id=${bug_id}
  Example: --proj_home=/home/user --proj_name=chart --bug_id=1
  Another: --proj_home=/home/user --proj_name=chart --bug_id=1,4,8
  ```

**OPTION 1** : run within eclipse.

* From the Main class:

   `Run As`→`Run Configurations…` →`Arguments` : set the above arguments as *Program Arguments*.

**OPTION 2** : run using command line.

* We also provide runnable jar file of *SimFix* in the home folder of the project `i.e., simfix.jar`.

  set the home directory of the *SimFix* project as your correct path and then run as:

  `java -jar simfix.jar --proj_home=/home/user --proj_name=chart --bug_id=1`

#### Step 3, Result Analysis

After finishing the repair, there will be two additional folders: `log` and `patch`.

* `log` : debug output, including buggy statements already tried, patches and reference code snippet for correct patch generation.

* `patch` : a single source file repaired by *SimFix* that can pass the test suite. In the source file, you can find the patch, which is formatted as (example of Chart_1):

  ```java
  // start of generated patch
  int index=this.plot.getIndexOf(this);
  CategoryDataset dataset=this.plot.getDataset(index);
  if(dataset==null){
  return result;
  }
  // end of generated patch
  /* start of original code
          int index = this.plot.getIndexOf(this);
          CategoryDataset dataset = this.plot.getDataset(index);
          if (dataset != null) {
              return result;
          }
   end of original code*/
  ```

## IV. Evaluation Result

Totally, *SimFix* successfully repair 40 bugs among 357 bugs in Defects4j v1.0 benchmark with generating 20 plausible but incorrect patches. The details are listed below.

* **Completely Correct Patch (35):** completely fix the bug.

  > Chart (4) : [1](./final/patch/chart/1/0/1_AbstractCategoryItemRenderer.java#L1795), [3](./final/patch/chart/3/0/1_TimeSeries.java#L626), [7](./final/patch/chart/7/0/1_TimePeriodValues.java#L299), [20](./final/patch/chart/20/0/1_ValueMarker.java#L95)
  >
  > Math (14) : [5](./final/patch/math/5/0/1_Complex.java#L304), [33](./final/patch/math/33/0/1_SimplexTableau.java#L338), [35-L1](./final/patch/math/35/0/1_ElitisticListPopulation.java#L50), [35-L2](./final/patch/math/35/1/1_ElitisticListPopulation.java#L64), [41](./final/patch/math/41/0/1_Variance.java#L520), [50](./final/patch/math/50/0/1_BaseSecantSolver.java#L187), [53](./final/patch/math/53/0/1_Complex.java#L152), [57](./final/patch/math/57/0/1_KMeansPlusPlusClusterer.java#L175), [59](./final/patch/math/59/0/1_FastMath.java#L3482), [63](./final/patch/math/63/0/1_MathUtils.java#L417), [70](./final/patch/math/70/0/1_BisectionSolver.java#L72), [71-L1](./final/patch/math/71/0/1_RungeKuttaIntegrator.java#L174), [71-L2](./final/patch/math/71/2/1_EmbeddedRungeKuttaIntegrator.java#L294), [75](./final/patch/math/75/0/1_Frequency.java#L303), [79](./final/patch/math/79/0/1_MathUtils.java#L1624), [98-L1](./final/patch/math/98/0/1_BigMatrixImpl.java#L989), [98-L2](./final/patch/math/98/4/1_RealMatrixImpl.java#L776)
  >
  > Lang (10) : [7](./final/patch/lang/7/0/1_NumberUtils.java#L452), [10](./final/patch/lang/10/0/1_FastDateParser.java#L306), [27](./final/patch/lang/27/0/1_NumberUtils.java#L488), [33](./final/patch/lang/33/0/1_ClassUtils.java#L909), [35-L1](./final/patch/lang/35/0/1_ArrayUtils.java#L3292), [35-L2](./final/patch/lang/35/1/1_ArrayUtils.java#L3571), [39](./final/patch/lang/39/0/1_StringUtils.java#L3675), [41-L1](./final/patch/lang/41/0/1_ClassUtils.java#L169), [41-L2](./final/patch/lang/41/13/1_ClassUtils.java#L229), [43](./final/patch/lang/43/0/1_ExtendedMessageFormat.java#L419), [58](./final/patch/lang/58/0/1_NumberUtils.java#L452), [60-L1](./final/patch/lang/60/0/1_StrBuilder.java#L1673), [60-L2](./final/patch/lang/60/1/1_StrBuilder.java#L1730)
  >
  > Closure (6) : [14](./final/patch/closure/14/0/1_ControlFlowAnalysis.java#L766), [57](./final/patch/closure/57/0/1_ClosureCodingConvention.java#L197), [62](./final/patch/closure/62/0/1_LightweightMessageFormatter.java#L97), [63](./final/patch/closure/63/0/1_LightweightMessageFormatter.java#L97), [73](./final/patch/closure/73/0/1_CodeGenerator.java#L1045), [115](./final/patch/closure/115/0/1_FunctionInjector.java#L730)
  >
  > Time (1) : [7](./final/patch/time/7/0/1_DateTimeFormatter.java#L708)

* **Partially Correct Path (5):** generate at least one correct patch for one location for multiple location bug.

  > Chart (2) : [18](./final/patch/chart/18/1/1_DefaultKeyedValues.java#L334), [22](./final/patch/chart/22/0/1_KeyedObjects2D.java#L344)
  >
  > Math (1) : [72](./final/patch/math/72/0/1_BrentSolver.java#L114)
  >
  > Closure (2) : [68](./final/patch/closure/68/0/1_JsDocInfoParser.java#L1760), [79-L1](./final/patch/closure/79/3/1_VarCheck.java#L249), [79-L2](./final/patch/closure/79/4/1_VarCheck.java#L134)

* **Incorrect Patch (20):**

  > Chart (3) : [12](./final/patch/chart/12/0/1_MultiplePiePlot.java#L145), [14](./final/patch/chart/14/3/1_CategoryPlot.java#L2440), [25](./final/patch/chart/25/0/1_DatasetUtilities.java#L576)
  >
  > Math (12) : [1](./final/patch/math/1/0/1_BigFraction.java#L303), [6](./final/patch/math/6/0/1_BaseOptimizer.java#L93), [8](./final/patch/math/8/0/1_DiscreteDistribution.java#L189), [20](./final/patch/math/20/0/1_CMAESOptimizer.java#L994), [28](./final/patch/math/28/0/1_SimplexSolver.java#L123), [40](./final/patch/math/40/0/1_BracketingNthOrderBrentSolver.java#L326), [73](./final/patch/math/73/0/1_BrentSolver.java#L132), [80](./final/patch/math/80/0/1_EigenDecompositionImpl.java#L1137), [81](./final/patch/math/81/0/1_EigenDecompositionImpl.java#L1535), [82](./final/patch/math/82/0/1_SimplexSolver.java#L62), [85](./final/patch/math/85/0/1_UnivariateRealSolverUtils.java#L198), [88](./final/patch/math/88/0/1_SimplexSolver.java#L83)
  >
  > Lang (3) : [44](./final/patch/lang/44/0/1_NumberUtils.java#L193), [45](./final/patch/lang/45/0/1_WordUtils.java#L622), [63](./final/patch/lang/63/0/1_DurationFormatUtils.java#L436)
  >
  > Closure (1) : [106](./final/patch/closure/106/2/1_JsDocInfoParser.java#L1307)
  >
  > Time (1) : [9](./final/patch/time/9/0/1_IllegalFieldValueException.java#L45)

## V. Structure of the project
```powershell
  |--- README.md   :  user guidance
  |--- bin         :  binary code
  |--- d4j-info    :  defects4j information
  |--- doc         :  document
  |--- final       :  evaluation result
  |--- lib         :  dependent libraries
  |--- sbfl        :  fault localization tool
  |--- simfix.jar  :  runnable jar file
  |--- src         :  source code
  |--- test        :  test suite
```

  ----


<u>__ALL__ suggestions are welcomed.</u>