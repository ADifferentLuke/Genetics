package net.lukemcomber.genetics.utilities.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class SimpleSimulation {

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
    @JsonProperty("reusePopulation")
    public int reusePopulation;
}
