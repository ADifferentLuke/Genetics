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

Personal [notes](https://github.com/ADifferentLuke/Genetics/tree/main/notes). <br /><br />

#### Fitness:
![Basic Fitness Function](https://github.com/ADifferentLuke/Genetics/blob/main/misc/BasicFitnessFunction.png?raw=true)

#### Simulations:
[Simulation 3 : Grass (4x zoom)](https://github.com/ADifferentLuke/Genetics/blob/main/notes/simulation_3_800x400) <br/>
![Simulation Grass](https://github.com/ADifferentLuke/Genetics/blob/main/misc/Grass.gif?raw=true)

#### Examples
For a pre-canned universe, see [Flat World](https://github.com/ADifferentLuke/Genetics/blob/main/src/main/java/net/lukemcomber/genetics/universes/FlatFloraUniverse.java)

For an example UI implementation, see [Oracle](https://github.com/ADifferentLuke/Oracle)</br>
For an example CLI implementation, see [SimpleSimulator](https://github.com/ADifferentLuke/Genetics/blob/main/src/main/java/net/lukemcomber/genetics/utilities/SimpleSimulator.java) <br />

Javadoc can be found [here.](https://www.javadoc.io/doc/net.lukemcomber/genetics/latest/index.html)</br>
#### Maven
    <dependency>
        <groupId>net.lukemcomber</groupId>
        <artifactId>genetics</artifactId>
        <version>v0.2.2</version>
    </dependency>

#### Sources used:
* Asexual Versus Sexual Reproduction in Genetic Algorithms1
  * https://carleton.ca/cognitivescience/wp-content/uploads/2006-09.pdf
* A simple algorithm for optimization and model fitting
  * https://www.aanda.org/articles/aa/full_html/2009/27/aa11740-09/aa11740-09.html
* Using Genetic Algorithms with Asexual Transposition
  * https://dl.acm.org/doi/pdf/10.5555/2933718.2933761


#### Outstanding Items:
* Change metabolism to gradual spend
* Resource tracking
* Morbid resource generation
* Multidimensional resource gathering
* Make Env metadata sample rate configurable


@author: Luke McOmber  
@License: MIT License (see LICENSE)


