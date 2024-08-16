package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

/**
 * Represent a temporal coordinate in the simulation
 *
 * @param totalTicks  total ticks occurred
 * @param totalDays   total days occurred
 * @param currentTick current tick of the day
 */
public record TemporalCoordinates(long totalTicks, long totalDays, long currentTick) {

    /**
     * Returns a string representation of the temporal coordinates
     *
     * @return serialized coordinates
     */
    public String toString() {
        return String.format("(%d,%d,%d)", totalTicks(), totalDays(), currentTick());
    }

}
