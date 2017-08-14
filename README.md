# SimFix

*SimFix* is an automatic program repair technique, which leverages similar code snippets in the same project to generate patches. The following figure is the workflow of our approach.

![The workflow of this technique.\label{workflow}](./doc/figure/overview.png)

## I. Environment

* OS: Linux (tested on Ubuntu 16.04.2 LTS)
* Download and configure [Defects4J](https://github.com/rjust/defects4j) running environment.
* Configure the following path.
  * DEFECTS4J_HOME="home_of_defects4j"



## II. How to run

*SimFix* was traditionally developed as an Eclipse Java project, you can simply import this project to your workspace and run it as a common Java program. The main class is **cofix.main.Main**, and the running option please refer to the [Running Oprions](#step-2-running-options).



#### Step 1, Build The Project



#### Step 2, Running Options 



#### Step 3, Result Analysis



## III. Evaluation Result

