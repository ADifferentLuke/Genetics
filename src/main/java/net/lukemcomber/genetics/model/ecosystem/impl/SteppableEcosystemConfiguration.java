package net.lukemcomber.genetics.model.ecosystem.impl;

import lombok.Builder;
import lombok.Getter;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.util.Map;

@Builder
@Getter
public class SteppableEcosystemConfiguration {

    private long maxDays;

    private int ticksPerDay;
    private SpatialCoordinates size;
    private String name;
    private long ticksPerTurn;
    private Map<SpatialCoordinates, String> startOrganisms;

}
