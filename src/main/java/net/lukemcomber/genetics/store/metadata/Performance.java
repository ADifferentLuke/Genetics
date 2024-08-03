package net.lukemcomber.genetics.store.metadata;

import net.lukemcomber.genetics.store.Indexed;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.Searchable;

@Searchable
public class Performance implements Metadata {

    public static final String PROPERTY_PERFORMANCE_ENABLE = "metadata.Performance.enabled";

    @Indexed(name = "name")
    public String name;

    @Indexed(name = "parent")
    public String parentId;

    @Indexed(name = "genome")
    public String dna = "";

    public Long age;
    public Long birthTick;
    public Integer offspring; //Number of seeds

    public Integer cells;
    public Integer totalEnergyHarvested;
    public Integer totalEnergyMetabolized;

    //Must be called at death?
    public String causeOfDeath;
    public Integer deathEnergy;
    public Long deathTick;

    @Indexed(name = "fitness")
    public Double fitness;

}
