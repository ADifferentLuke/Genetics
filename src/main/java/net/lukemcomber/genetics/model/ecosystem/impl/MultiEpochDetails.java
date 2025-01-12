package net.lukemcomber.genetics.model.ecosystem.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.lukemcomber.genetics.model.ecosystem.EcosystemDetails;

public class MultiEpochDetails extends EcosystemDetails {

    @JsonProperty("maxDays")
    private long maxDays;
    @JsonProperty("tickDelay")
    private long tickDelay;

    @JsonProperty("epochs")
    private int epochs;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("deleteFilterOnCompletion")
    private boolean deleteFilterOnExit;

    public long getMaxDays() {
        return maxDays;
    }

    public void setMaxDays(final long maxDays) {
        this.maxDays = maxDays;
    }

    public long getTickDelay() {
        return tickDelay;
    }

    public void setTickDelay(final long tickDelay) {
        this.tickDelay = tickDelay;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(final int epochs) {
        this.epochs = epochs;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public boolean isDeleteFilterOnExit() {
        return deleteFilterOnExit;
    }

    public void setDeleteFilterOnExit(final boolean deleteFilterOnExit) {
        this.deleteFilterOnExit = deleteFilterOnExit;
    }
}
