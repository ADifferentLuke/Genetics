package net.lukemcomber.genetics.model.ecosystem;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * Configuration options for simulations
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class EcosystemDetails {

    /**
     * Get the simulations id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the simulations id
     *
     * @param id id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the human-readable simulation name
     *
     * @return simulation name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the human-readable simulation name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the width of the simulation
     *
     * @return width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width of the simulation
     *
     * @param width width in pixels
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gets the height of the simulation
     *
     * @return height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height of the simulation
     *
     * @param height height in pixels
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the depth of the simulation
     *
     * @return depth in pixels
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Sets the depth of the simulation
     *
     * @param depth depth in pixels
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Get if the simulation is active
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the simulation active-ness
     *
     * @param active value to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get if the simulation is interactive
     *
     * @return true if interactive
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Sets if the simulation is interactive
     *
     * @param interactive value to set
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Get total number of ticks the simulation has run
     *
     * @return total ticks of the simulation
     */
    public long getTotalTicks() {
        return totalTicks;
    }

    /**
     * Sets the total ticks of the simulation
     *
     * @param totalTicks ticks in simulation
     */
    public void setTotalTicks(long totalTicks) {
        this.totalTicks = totalTicks;
    }

    /**
     * Get the current tick of the day
     *
     * @return tick of the day
     */
    public long getCurrentTick() {
        return currentTick;
    }

    /**
     * Sets the current tick of the day
     *
     * @param currentTick tick to set
     */
    public void setCurrentTick(long currentTick) {
        this.currentTick = currentTick;
    }

    /**
     * Get total number of days that have occurred
     *
     * @return days passed
     */
    public long getTotalDays() {
        return totalDays;
    }

    /**
     * Sets the total number of days that have occurred
     *
     * @param totalDays days to set
     */
    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }

    /**
     * Gets the number of current living organisms in the simulation
     *
     * @return living organisms
     */
    public long getCurrentOrganismCount() {
        return currentOrganismCount;
    }

    /**
     * Sets the number of living organisms in the simulation
     *
     * @param currentOrganismCount current count
     */
    public void setCurrentOrganismCount(long currentOrganismCount) {
        this.currentOrganismCount = currentOrganismCount;
    }

    /**
     * Gets the total number of organisms that have existed
     *
     * @return total organisms existed
     */
    public long getTotalOrganismCount() {
        return totalOrganismCount;
    }

    /**
     * Sets the total number of organisms that have existed
     *
     * @param totalOrganismCount count to set
     */
    public void setTotalOrganismCount(long totalOrganismCount) {
        this.totalOrganismCount = totalOrganismCount;
    }

    /**
     * Get simulation parameters
     *
     * @return map of key value pairs
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets the parameters of the simulation
     *
     * @param properties name value pairs
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Get organism genomes to seed the simulation
     *
     * @return list of genomes
     */
    public List<String> getInitialPopulation() {
        return initialPopulation;
    }

    /**
     * Sets the genomes of the organisms used to seed the simulaion
     *
     * @param initialPopulation list of genomes
     */
    public void setInitialPopulation(List<String> initialPopulation) {
        this.initialPopulation = initialPopulation;
    }


    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("width")
    private int width;
    @JsonProperty("height")
    private int height;
    @JsonProperty("depth")
    private int depth;
    @JsonProperty("active")
    private boolean active;
    @JsonProperty("interactive")
    private boolean interactive;
    @JsonProperty("totalTicks")
    private long totalTicks;
    @JsonProperty("currentTick")
    private long currentTick;
    @JsonProperty("totalDays")
    private long totalDays;
    @JsonProperty("currentOrganismCount")
    private long currentOrganismCount;
    @JsonProperty("totalOrganismCount")
    private long totalOrganismCount;
    @JsonProperty("properties")
    private Map<String, String> properties;
    @JsonProperty("initialPopulation")
    private List<String> initialPopulation;

}
