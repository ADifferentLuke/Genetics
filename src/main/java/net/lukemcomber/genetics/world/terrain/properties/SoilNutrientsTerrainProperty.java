package net.lukemcomber.genetics.world.terrain.properties;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.world.terrain.TerrainProperty;

/**
 * A {@link TerrainProperty} for soil nutrients
 */
public class SoilNutrientsTerrainProperty implements TerrainProperty<Integer> {

    public static final String ID = "SOIL_NUTRIENTS";
    private Integer value;

    /**
     * Initialize the property
     */
    public SoilNutrientsTerrainProperty() {
        value = null;
    }

    /**
     * Initialize the property and set the value
     *
     * @param value value to set
     */
    public SoilNutrientsTerrainProperty(final int value) {
        this.value = value;
    }

    /**
     * Get the current value of the property
     *
     * @return value
     */
    @Override
    public Integer getValue() {
        return value;
    }

    /**
     * Sets the value of the property
     *
     * @param integer the value to set
     */
    @Override
    public void setValue(final Integer integer) {
        value = integer;
    }

    /**
     * Sets the value of the property from a string
     *
     * @param s value to set
     */
    @Override
    public void setValue(final String s) {
        value = Integer.parseInt(s);
    }

    /**
     * Get the ID of the property
     *
     * @return {@link SoilNutrientsTerrainProperty#ID}
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * Return a clone of the property
     *
     * @return the clone
     */
    @Override
    public TerrainProperty<Integer> clone() {
        if (null == value) {
            return new SoilNutrientsTerrainProperty();
        } else {
            return new SoilNutrientsTerrainProperty(value);
        }
    }
}
