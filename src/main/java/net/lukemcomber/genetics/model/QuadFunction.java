package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */


/**
 * A generic Quad Function interface
 * @param <A> - IN 1
 * @param <B> - IN 2
 * @param <C> - IN 3
 * @param <D> - IN 4
 * @param <R> - OUT
 */
@FunctionalInterface
public interface QuadFunction<A,B,C,D,R> {

    /**
     * Apply the function and return the redult
     * @param a Input parameter
     * @param b Input parameter
     * @param c Input parameter
     * @param d Input parameter
     * @return result
     */
    R apply(A a, B b, C c, D d);
}
