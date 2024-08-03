# Genetics

A library to simulate genetic evolution.

## Goal:
I have several goals that I'm looking to accomplish with this library
1. Learn about genetic algorithms
2. Play around with different designs patterns
	- Seeing what works and what doesn't
	- Manage dependencies without Spring or DI
3. Play with language features and interesting patterns
3. For fun and curiosity!

Fitness:
$$f(x)= (W_{1}log(\sqrt{C_{1}}) + W_{2}\frac{1}{1 + e^{|D_{1}|}} + W_{3}\frac{1}{H_{1} - M_{1}})log(W_{4}O_{1})

For an example implementation, see [Oracle](https://github.com/ADifferentLuke/Oracle)</br>
For a pre-canned universe, see [Flat World](https://github.com/ADifferentLuke/Genetics/blob/main/src/main/java/net/lukemcomber/genetics/universes/FlatFloraUniverse.java)

Personal notes: [notes/](https://github.com/ADifferentLuke/Genetics/tree/main/notes)
Analysis: [analysis/]()


Sources used:
* Asexual Versus Sexual Reproduction in Genetic Algorithms1
  * https://carleton.ca/cognitivescience/wp-content/uploads/2006-09.pdf
* A simple algorithm for optimization and model fitting
  * https://www.aanda.org/articles/aa/full_html/2009/27/aa11740-09/aa11740-09.html
* Using Genetic Algorithms with Asexual Transposition
  * https://dl.acm.org/doi/pdf/10.5555/2933718.2933761

@author: Luke McOmber  
@License: MIT License (see LICENSE)


