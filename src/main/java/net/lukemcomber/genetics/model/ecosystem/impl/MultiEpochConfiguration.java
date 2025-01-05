package net.lukemcomber.genetics.model.ecosystem.impl;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import lombok.Builder;
import lombok.Getter;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.util.Map;



@Builder
@Getter
public class MultiEpochConfiguration {

    private SpatialCoordinates size;

    private String name;
    private String fileFilterPath;

    private int ticksPerDay;
    private int epochs;
    private int initialPopulation;
    private int tickDelayMs;
    private int reusePopulation;

    private long maxDays;

    private boolean deleteFilterOnExit;

    private Map<SpatialCoordinates, String> startOrganisms;

}
