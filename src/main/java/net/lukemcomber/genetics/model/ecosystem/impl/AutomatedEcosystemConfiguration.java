package net.lukemcomber.genetics.model.ecosystem.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.ecosystem.EcosystemConfiguration;

public class AutomatedEcosystemConfiguration extends EcosystemConfiguration {
    @JsonProperty("maxDays")
    public long maxDays;
    @JsonProperty("tickDelay")
    public long tickDelay;
}
