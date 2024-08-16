package net.lukemcomber.genetics.exception;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

/**
 * This class is a wrapper around {@link RuntimeException} that has
 * a human-readable and displayable error message.
 * <p>
 * It provides a way for lower level classes to bubble up errors exceptions while
 * the higher level callers can determine whether to show the exception message to the user
 */
public class EvolutionException extends RuntimeException {

    /**
     * Creates a new instance from a Throwable
     *
     * @param throwable
     */
    public EvolutionException(final Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new instance with the exception message set
     *
     * @param message message to set
     */
    public EvolutionException(final String message) {
        super(message);
    }
}
