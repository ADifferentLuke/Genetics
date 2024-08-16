package net.lukemcomber.genetics.model.ecosystem.impl;

/*
 * (c) 2024 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;

/**
 * Configuration options for interactive simulations
 */
public class SteppableEcosystemConfiguration extends EcosystemConfiguration {
    /**
     * Gets the number of ticks to batch
     *
     * @return tick batch size
     */
    public long getTurnsPerTick() {
        return turnsPerTick;
    }

    /**
     * Sets the number of ticks to batch
     *
     * @param turnsPerTick tick batch size
     */
    public void setTurnsPerTick(long turnsPerTick) {
        this.turnsPerTick = turnsPerTick;
    }

    @JsonProperty("turnsPerTick")
    private long turnsPerTick;
}
