package net.lukemcomber.dev.ai.genetics.world.terrain.properties;

import net.lukemcomber.dev.ai.genetics.world.terrain.TerrainProperty;

public class SoilToxicityTerrainProperty implements TerrainProperty<Integer> {

    public static final String ID = "SOIL_TOXICITY";
    private Integer value;

    public SoilToxicityTerrainProperty() {
        value = null;
    }

    public SoilToxicityTerrainProperty(final int value) {
        this.value = value;
    }

    /**
     * @return
     */
    @Override
    public Integer getValue() {
        return value;
    }

    /**
     * @param integer the value to set
     */
    @Override
    public void setValue(Integer integer) {
        value = integer;
    }

    /**
     * @param s
     */
    @Override
    public void setValue(String s) {
       value = Integer.parseInt(s);
    }

    /**
     * @return
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * @return
     */
    @Override
    public TerrainProperty<Integer> clone() {
        if (null == value) {
            return new SoilToxicityTerrainProperty();
        } else {
            return new SoilToxicityTerrainProperty(value);
        }
    }
}
