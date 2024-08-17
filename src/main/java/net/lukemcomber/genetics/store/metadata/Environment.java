package net.lukemcomber.genetics.store.metadata;

import net.lukemcomber.genetics.store.Metadata;

/**
 * {@link Metadata} to track population growth over time
 */
public class Environment implements Metadata {

    public static final String PROPERTY_PERFORMANCE_ENABLE = "metadata.Environment.enabled";
    private Long tickCount;
    private Long totalOrganisms;

    /**
     * Get the current tick
     *
     * @return tick
     */
    public Long getTickCount() {
        return tickCount;
    }

    /**
     * Sets the current tick
     *
     * @param tickCount tick to set
     */
    public void setTickCount(Long tickCount) {
        this.tickCount = tickCount;
    }

    /**
     * Get the count of alive organisms
     *
     * @return count
     */
    public Long getTotalOrganisms() {
        return totalOrganisms;
    }

    /**
     * Sets the total count of organisms alive
     *
     * @param totalOrganisms count to set
     */
    public void setTotalOrganisms(Long totalOrganisms) {
        this.totalOrganisms = totalOrganisms;
    }
}
