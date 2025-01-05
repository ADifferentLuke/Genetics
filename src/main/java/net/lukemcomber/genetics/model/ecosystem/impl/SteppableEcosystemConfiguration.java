package net.lukemcomber.genetics.model.ecosystem.impl;

import lombok.Builder;
import lombok.Getter;
import net.lukemcomber.genetics.model.SpatialCoordinates;

@Builder
@Getter
public class SteppableEcosystemConfiguration {

    private long maxDays;

    private int ticksPerDay;
    private SpatialCoordinates size;
    private String name;
    private long ticksPerTurn;
}
