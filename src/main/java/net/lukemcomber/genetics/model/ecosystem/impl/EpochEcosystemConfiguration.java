package net.lukemcomber.genetics.model.ecosystem.impl;

import lombok.Builder;
import lombok.Getter;
import net.lukemcomber.genetics.model.SpatialCoordinates;

@Builder
@Getter
public class EpochEcosystemConfiguration {

    private long maxDays;
    private long tickDelayMs;

    private int ticksPerDay;
    private SpatialCoordinates size;
    private String type;
    private String name;
}
