package net.lukemcomber.genetics.store.metadata;

import net.lukemcomber.genetics.store.Metadata;

public class Performance implements Metadata {

    public static final String PROPERTY_PERFORMANCE_ENABLE = "metadata.Performance.enabled";

    public String name;

    public Long age;
    public Long birthTick;
    public Integer offspring; //Number of seeds


    //Must be called at death?
    public String causeOfDeath;
    public Integer deathEnergy;
    public Long deathTick;

    public Float fitness;
}
