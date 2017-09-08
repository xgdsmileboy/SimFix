# SimFix

* [I. Introduction of SimFix](#user-content-i-introduction)
* [II. Environment setup](#user-content-ii-environment)
* [III. Run SimFix Step-by-Step](#user-content-iii-how-to-run)
* [IX. Evaluation Result](#user-content-ix-evaluation-result)

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

*SimFix* was traditionally developed as an Eclipse Java project, you can simply import this project to your workspace and run it as a common Java program. The main class is **cofix.main.Main**, and the running option please refer to the [Running Options](#user-content-step-2-running-options).

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

* *-\-proj_home* : the home of buggy program of benchmark. (`${buggy_program_path}` for the example)

* *-\-proj_name* : the project name of buggy program of benchmark. (`chart` for the example)

* *-\-bug_id* : the identifier of the buggy program. (`1` for the example)

  * the option of *-\-bugy_id* supports multiple formats:

    `single_id` : repair single bug, `e.g., 1`.

    `startId-endId` : repair a series of bugs with consecutive identifiers, `e.g., 1-3`.

    `single_id,single_id,single_id` : repair any bugs for the specific program, `e.g., 1,5,9`.

    `all` : repair all buggy versions of a specific project, `i.e., all`.

  ```shell
  Usage: --proj_home=${proj_home} --proj_name=${proj_name} --bug_id=${bug_id}
  Example: --proj_home=/home/user --proj_name=chart --bug_id=1
  Another: --proj_home=/home/user --proj_name=chart --bug_id=1,4,8
  ```

**OPTION 1** : running using eclipse.

**OPTION 2** : running using command line.

#### Step 3, Result Analysis



## IX. Evaluation Result

####  Overview