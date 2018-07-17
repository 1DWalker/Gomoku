# Gomoku
A Gomoku AI and an AI testing framework. 

The AI uses Monte Carlo Tree Search.
http://mcts.ai/about/

The AI uses various enhancements such as:
Rapid Action Value Estimation
http://www.cs.utexas.edu/~pstone/Courses/394Rspring13/resources/mcrave.pdf

Decisive and anti-decisive moves
https://ieeexplore.ieee.org/document/5593334/

Progressive Bias
http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.106.3015&rep=rep1&type=pdf

As the branching factor of gomoku is very high (210), the AI aggressively prunes the search space, only considering moves that are near other stones, until a certain simulation threshold is reached. 

The testing is done using the sequential probability ratio test. 
https://projecteuclid.org/download/pdf_1/euclid.aoms/1177731118
This is a test where the sample size is not fixed in advanced. Instead, the testing is terminated when significant results are observed.
