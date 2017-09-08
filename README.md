# SimFix

* [I. Introduction of SimFix](#user-content-i-introduction)
* [II. Environment setup](#user-content-ii-environment)
* [III. Run SimFix Step-by-Step](#user-content-iii-how-to-run)
* [IX. Evaluation Result](#user-content-ix-evaluation-result)

## I. Introduction

*SimFix* is an automatic program repair technique, which leverages similar code snippets in the same project to generate patches. The following figure is the workflow of our approach.

![The workflow of this technique.\label{workflow}](./doc/figure/overview.png)

## II. Environment

* OS: Linux (Tested on Ubuntu 16.04.2 LTS)
* Download and configure [Defects4J](https://github.com/rjust/defects4j) running environment.
* Configure the following path.
  * DEFECTS4J_HOME="home_of_defects4j"



## III. How to run

*SimFix* was traditionally developed as an Eclipse Java project, you can simply import this project to your workspace and run it as a common Java program. The main class is **cofix.main.Main**, and the running option please refer to the [Running Options](#user-content-step-2-running-options).

#### Before running

```shell
unzip file sbfl/data.zip to sbfl/data  ## used for fault localization
```

#### Step 1, Build The Project



#### Step 2, Running Options 



#### Step 3, Result Analysis



## IX. Evaluation Result

####  Overview