package net.lukemcomber.genetics.biology;

import net.lukemcomber.genetics.biology.plant.PlantBehavior;

import java.util.LinkedList;
import java.util.List;

public class TestGenome extends Genome {


    public TestGenome(final int num) {
        super(num, "TestGenome");
    }

    public TestGenome(final List<Gene> list) {
        super(list, "TestGenome");
    }

    @Override
    public PlantBehavior getNextAct() {
        return null;
    }

    @Override
    public Genome clone() {
        final List<Gene> geneCopy = new LinkedList<>();
        for (int i = 0; i < getNumberOfGenes(); ++i) {
            geneCopy.add(new Gene(getGeneNumber(i)));
        }
        return new TestGenome(geneCopy);
    }
}
