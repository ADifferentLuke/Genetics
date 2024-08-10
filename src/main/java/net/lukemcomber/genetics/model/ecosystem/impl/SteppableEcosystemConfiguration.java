package net.lukemcomber.genetics.model.ecosystem.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;

public class SteppableEcosystemConfiguration extends EcosystemConfiguration {
    @JsonProperty("turnsPerTick")
    public long turnsPerTick;
}
