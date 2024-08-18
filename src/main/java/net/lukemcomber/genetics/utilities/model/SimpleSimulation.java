package net.lukemcomber.genetics.utilities.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.SpatialCoordinates;
import net.lukemcomber.genetics.utilities.SimpleSimulator;

import java.util.Map;

/**
 * Configuration for {@link SimpleSimulator}
 */
public class SimpleSimulation {

    @JsonProperty("name")
    private String name;
    @JsonProperty("epochs")
    private int epochs;
    @JsonProperty("height")
    private int height;
    @JsonProperty("width")
    private int width;
    @JsonProperty("ticksPerDay")
    private int ticksPerDay;
    @JsonProperty("maxDaysPerEpoch")
    private long maxDays;
    @JsonProperty("initialPopulation")
    private int initialPopulation;
    @JsonProperty("tickDelayMs")
    private int tickDelayMs;
    @JsonProperty("reusePopulation")
    private int reusePopulation;

    @JsonProperty("startOrganisms")
    private Map<SpatialCoordinates, String> startOrganisms;

    /**
     * Get simulation name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets simulation name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the number of epochs
     *
     * @return number of epochs
     */
    public int getEpochs() {
        return epochs;
    }

    /**
     * Set the number of epochs
     *
     * @param epochs
     */
    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    /**
     * Get simulations height
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the simulation height
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the simulation's width
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the simulation's width
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get ticks per day
     *
     * @return ticks per day
     */
    public int getTicksPerDay() {
        return ticksPerDay;
    }

    /**
     * Set ticks per day
     *
     * @param ticksPerDay
     */
    public void setTicksPerDay(int ticksPerDay) {
        this.ticksPerDay = ticksPerDay;
    }

    /**
     * Get maximum number of days
     *
     * @return max number of days
     */
    public long getMaxDays() {
        return maxDays;
    }

    /**
     * Sets the maximum number of days
     *
     * @param maxDays
     */
    public void setMaxDays(long maxDays) {
        this.maxDays = maxDays;
    }

    /**
     * Get the initial population count
     *
     * @return initial population count
     */
    public int getInitialPopulation() {
        return initialPopulation;
    }

    /**
     * Set the initial population count
     *
     * @param initialPopulation
     */
    public void setInitialPopulation(int initialPopulation) {
        this.initialPopulation = initialPopulation;
    }

    /**
     * Get the tick delay in milliseconds
     *
     * @return tick delay
     */
    public int getTickDelayMs() {
        return tickDelayMs;
    }

    /**
     * Sets the tick delay in milliseconds
     *
     * @param tickDelayMs
     */
    public void setTickDelayMs(int tickDelayMs) {
        this.tickDelayMs = tickDelayMs;
    }

    /**
     * Get count of population to reuse over epochs
     *
     * @return reincarnation count
     */
    public int getReusePopulation() {
        return reusePopulation;
    }

    /**
     * Set count of population to reuse over epochs
     *
     * @param reusePopulation
     */
    public void setReusePopulation(int reusePopulation) {
        this.reusePopulation = reusePopulation;
    }

    /**
     * Gets the organisms and coordinates to start simulation
     *
     * @return map of coordinates to genome strings
     */
    public Map<SpatialCoordinates, String> getStartOrganisms() {
        return startOrganisms;
    }

    /**
     * Sets the organisms and coordinates to start simulation
     *
     * @param startOrganisms
     */
    public void setStartOrganisms(Map<SpatialCoordinates, String> startOrganisms) {
        this.startOrganisms = startOrganisms;
    }

}
