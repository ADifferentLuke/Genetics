package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

public record TemporalCoordinates(long totalTicks, long totalDays, long currentTick ) {

    public String toString(){
        return String.format("(%d,%d,%d)", totalTicks(),totalDays(),currentTick());
    }

}
