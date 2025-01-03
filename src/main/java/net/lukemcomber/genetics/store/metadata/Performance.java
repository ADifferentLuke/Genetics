package net.lukemcomber.genetics.store.metadata;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.store.Indexed;
import net.lukemcomber.genetics.store.Metadata;
import net.lukemcomber.genetics.store.Primary;
import net.lukemcomber.genetics.store.Searchable;

public class Performance implements Metadata {

    public static final String PROPERTY_PERFORMANCE_ENABLE = "metadata.Performance.enabled";

    /**
     * Get the organisms name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the organisms name
     *
     * @param name name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the parent id
     * @return parent id
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parent id
     * @param parentId parentId to set
     */
    public void setParentId(final String parentId) {
        this.parentId = parentId;
    }

    /**
     * Get the serialized genome
     * @return genome as a string
     */
    public String getDna() {
        return dna;
    }

    /**
     * Sets the serialized genome
     * @param dna genome to set
     */
    public void setDna(final String dna) {
        this.dna = dna;
    }

    /**
     * Get the cause of death identifier
     * @return cause of death id
     */
    public Integer getCauseOfDeath() {
        return causeOfDeath;
    }

    /**
     * Sets the cause of death identifier
     * @param causeOfDeath cause of death id to set
     */
    public void setCauseOfDeath(final Integer causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }

    /**
     * Get the organism's age
     * @return age
     */
    public Long getAge() {
        return age;
    }

    /**
     * Sets the organism's age
     * @param age age to set
     */
    public void setAge(final Long age) {
        this.age = age;
    }

    /**
     * Get birth tick
     * @return birth tick
     */
    public Long getBirthTick() {
        return birthTick;
    }

    /**
     * Sets the birth tick
     * @param birthTick tick to set
     */
    public void setBirthTick(final Long birthTick) {
        this.birthTick = birthTick;
    }

    /**
     * Get number of offspring
     * @return offspring count
     */
    public Integer getOffspring() {
        return offspring;
    }

    /**
     * Sets the number of offsprint
     * @param offspring
     */
    public void setOffspring(final Integer offspring) {
        this.offspring = offspring;
    }

    /**
     * Get total number of cells
     * @return total cells
     */
    public Integer getCells() {
        return cells;
    }

    /**
     * Sets the total number of cells
     * @param cells count to set
     */
    public void setCells(final Integer cells) {
        this.cells = cells;
    }

    /**
     * Get the total amount of energy harvest
     * @return total energy
     */
    public Integer getTotalEnergyHarvested() {
        return totalEnergyHarvested;
    }

    /**
     * Set the total amount of energy harvested
     * @param totalEnergyHarvested
     */
    public void setTotalEnergyHarvested(final Integer totalEnergyHarvested) {
        this.totalEnergyHarvested = totalEnergyHarvested;
    }

    /**
     * Get total energy metabolized
     * @return total energy metabolized
     */
    public Integer getTotalEnergyMetabolized() {
        return totalEnergyMetabolized;
    }

    /**
     * Sets the total amount of energy metabolized
     * @param totalEnergyMetabolized energy metabolized
     */
    public void setTotalEnergyMetabolized(final Integer totalEnergyMetabolized) {
        this.totalEnergyMetabolized = totalEnergyMetabolized;
    }

    /**
     * Get the cause of death reason
     * @return cause of death string
     */
    public String getCauseOfDeathStr() {
        return causeOfDeathStr;
    }

    /**
     * Sets the cause of death reason
     * @param causeOfDeathStr cause to set
     */
    public void setCauseOfDeathStr(final String causeOfDeathStr) {
        this.causeOfDeathStr = causeOfDeathStr;
    }

    /**
     * Get energy count at death
     * @return leftover energy
     */
    public Integer getDeathEnergy() {
        return deathEnergy;
    }

    /**
     * Sets the energy count at organism death
     * @param deathEnergy count to set
     */
    public void setDeathEnergy(final Integer deathEnergy) {
        this.deathEnergy = deathEnergy;
    }

    /**
     * Get the tick the organism died
     * @return death tick
     */
    public Long getDeathTick() {
        return deathTick;
    }

    /**
     * Sets the tick the organism died
     * @param deathTick tick of death
     */
    public void setDeathTick(final Long deathTick) {
        this.deathTick = deathTick;
    }

    /**
     * Get the fitness of the organism
     * @return fitness
     */
    public Double getFitness() {
        return fitness;
    }

    /**
     * Set the fitness of the organism
     * @param fitness fitness to set
     */
    public void setFitness(final Double fitness) {
        this.fitness = fitness;
    }

    @Indexed(name = "name")
    private String name;
    @Indexed(name = "parent")
    private String parentId;
    @Indexed(name = "genome")
    private String dna = "";
    @Primary(name = "fitness")
    private Double fitness;
    private Integer causeOfDeath;
    private Long age;
    private Long birthTick;
    private Integer offspring;
    private Integer cells;
    private Integer totalEnergyHarvested;
    private Integer totalEnergyMetabolized;
    private String causeOfDeathStr;
    private Integer deathEnergy;
    private Long deathTick;


}
