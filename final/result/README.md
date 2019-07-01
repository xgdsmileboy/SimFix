* Correct(34)
  * chart(4) : 1, 3, 7, 20
  * closure(6) : 14, 57, 62, 63, 73, 115
  * lang(9) : 16, 27, 33, 39, 41,43, 50, 58, 60
  * math(14) : 5, 33, 35, 41, 50, 53, 57, 59, 63, 70, 71, 75, 79, 98
  * time(1) : 7
* Incorrect(22)
  * chart(4): 12, 14, 18, 22
  * closure(3): 79, 106
  * lang(3): 10, 44, 45, 63
  * math(12): 1,6, 8, 20, 28, 72, 73, 80, 81, 82, 85, 88

34/(34+22) = 60.7%

* would not be fixed bugs without fine-grained AST matching and differencing though permit variable adaptation(17).
  * chart(1) : 1
  * closure(5) : 14, 57, 62, 63, 115
  * lang(3) : 27, 33, 60
  * math(7) : 33, 41, 50, 57, 59, 75, 98
  * time(1) : 7
  
######  Update (2019-5-29):
  * Update: 
      * correct : 33, incorrect 22, precision: 33/(33+21)=60.0%
  * Explanation:
    * The patch of lang-27 should be classified as incorrect since then the test input contains all three characters of '.', 'e', and 'E', the generated patch behaves different with the human patch. Thanks for the report of He Ye from KTH, Sweden.
    * The patch of closure-79 should be classified as non-relavent since it cannot pass all the test cases. Thanks for the report of Deheng Yang from NUDT, China.

