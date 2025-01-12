package net.lukemcomber.genetics.model.ecosystem.impl;

import lombok.Builder;
import lombok.Getter;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class EpochEcosystemConfiguration {

    private long maxDays;
    private long tickDelayMs;

    private int ticksPerDay;
    private SpatialCoordinates size;
    private String name;
    private Map<SpatialCoordinates, String> startOrganisms;

}
