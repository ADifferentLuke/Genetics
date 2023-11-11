package net.lukemcomber.dev.ai.genetics.model;

public record TemporalCoordinates(long totalTicks, long totalDays, long currentTick ) {

    public String toString(){
        return String.format("(%d,%d,%d)", totalTicks(),totalDays(),currentTick());
    }

}
