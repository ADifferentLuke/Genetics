# Genetics

A library to simulate genetic evolution.

## Goal:
I have several goals that I'm looking to accomplish with this library
1. Learn about genetic algorithms
2. Play around with different designs patterns
	- Seeing what works and what doesn't
	- Manage dependencies without Spring or DI
3. Play with language features and interesting patterns
4. For fun and curiosity!

Personal [notes](https://www.lukemcomber.net/categories/genesis/). <br /><br />

#### Fitness Functions

##### BasicFitnessFunction:


![Formula](https://latex.codecogs.com/png.latex?F%20=%20%5Cfrac%7BD_c%7D%7BN_d%7D%20%5Ccdot%20%28W_c%20%5Cln%7B%5Csqrt%7BC%7D%7D%20%2B%20W_e%20%5Cfrac%7B1%7D%7BE_h%20-%20E_m%7D%29%20%5Ccdot%20%5Cln%7B(W_o%20O%20%2B%201)%7D)

Where:
- \(F\) = fitness  
- \(D_c\) = cause of death (integer value)  
- \(N_d\) = total number of possible death causes (`Organism.CauseOfDeath.count`)  
- \(W_c\) = cellsWeight  
- \(C\) = performance.getCells()  
- \(W_e\) = energyEfficiencyWeight  
- \(E_h\) = total energy harvested  
- \(E_m\) = total energy metabolized  
- \(W_o\) = childrenWeight  
- \(O\) = offspring count  
- If \(E_h - E_m = 0\), the denominator is replaced with 1 to avoid division by zero.


##### BasicV2FitnessFunction:

$$
F = [C>1]\cdot \min\left(\frac{u}{a},\frac{a}{u}\right)\cdot \min\left(1,\max\left(0,\frac{\min(D,A_{\max})-B}{\max(1,A_{\max}-B)}\right)\right)\cdot e^{-\beta\left(\max(0,1-O)+\max(0,O-2)\right)}\cdot \frac{C}{C+k}
$$

Where:
- u = 1 + number of roots
- a = 1 + number of stems + leaves
- C = total cells
- B = birth tick
- D = death tick
- Amax = maximum age
- O = offspring count
- β = 3
- k = 50
- [C > 1] = 1 if C > 1, else 0


#### Examples
For a pre-canned universe, see [Flat World](https://github.com/ADifferentLuke/Genetics/blob/main/src/main/java/net/lukemcomber/genetics/universes/FlatFloraUniverse.java)

For a Genome Stepping Application, see [Gstep](https://github.com/ADifferentLuke/Gstep)) < br/>
For a OpenGL-powered visualization application, see [GeneGL](https://github.com/ADifferentLuke/genegl)  
For an example CLI implementation, see [SimpleSimulator](https://github.com/ADifferentLuke/Genetics/blob/main/src/main/java/net/lukemcomber/genetics/utilities/SimpleSimulator.java) <br />

(depreciated) ~~For an example UI implementation, see [Oracle](https://github.com/ADifferentLuke/Oracle)~~</br>

Javadoc can be found [here.](https://www.javadoc.io/doc/net.lukemcomber/genetics/latest/index.html)</br>
Latest Artifact can be found [here.](https://central.sonatype.com/artifact/net.lukemcomber/genetics)

#### Sources used:
* Asexual Versus Sexual Reproduction in Genetic Algorithms1
  * https://carleton.ca/cognitivescience/wp-content/uploads/2006-09.pdf
* A simple algorithm for optimization and model fitting
  * https://www.aanda.org/articles/aa/full_html/2009/27/aa11740-09/aa11740-09.html
* Using Genetic Algorithms with Asexual Transposition
  * https://dl.acm.org/doi/pdf/10.5555/2933718.2933761

@author: Luke McOmber  
@License: MIT License (see LICENSE)


