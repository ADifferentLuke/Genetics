package net.lukemcomber.genetics.model.ecosystem.impl;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;

/**
 * Configuration options for automated simulations
 */
public class AutomatedEcosystemConfiguration extends EcosystemConfiguration {
    /**
     * Get the max number of days per simulation
     *
     * @return days per simulation
     */
    public long getMaxDays() {
        return maxDays;
    }

    /**
     * Sets the max number of days per simulation
     *
     * @param maxDays days per simulation
     */
    public void setMaxDays(long maxDays) {
        this.maxDays = maxDays;
    }

    /**
     * Get the sleep delay in millis for every tick
     *
     * @return delay between ticks
     */
    public long getTickDelay() {
        return tickDelay;
    }

    /**
     * Set the sleep period between ticks in millis
     *
     * @param tickDelay delay between ticks
     */
    public void setTickDelay(long tickDelay) {
        this.tickDelay = tickDelay;
    }

    @JsonProperty("maxDays")
    private long maxDays;
    @JsonProperty("tickDelay")
    private long tickDelay;
}
