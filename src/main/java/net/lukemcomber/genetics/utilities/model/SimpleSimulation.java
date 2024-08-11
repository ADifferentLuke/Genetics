package net.lukemcomber.genetics.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleSimulation {

    @JsonProperty("name")
    public String name;
    @JsonProperty("epochs")
    public int epochs;
    @JsonProperty("height")
    public int height;
    @JsonProperty("width")
    public int width;
    @JsonProperty("ticksPerDay")
    public int ticksPerDay;
    @JsonProperty("maxDaysPerEpoch")
    public long maxDays;
    @JsonProperty("initialPopulation")
    public int initialPopulation;
    @JsonProperty("tickDelayMs")
    public int tickDelayMs;
    @JsonProperty("reusePopulation")
    public int reusePopulation;

    @JsonProperty("startOrganisms")
    public Map<SpatialCoordinates,String> startOrganisms;
}
