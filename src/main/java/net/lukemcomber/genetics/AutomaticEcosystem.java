package net.lukemcomber.genetics;

import net.lukemcomber.genetics.model.SpatialCoordinates;

import java.io.IOException;

public class AutomaticEcosystem extends Ecosystem {
    public AutomaticEcosystem(int ticksPerDay, SpatialCoordinates size, String type) throws IOException {
        super( ticksPerDay, size, type);
    }

    @Override
    public boolean advance() {
        // You can not advance an automatic ecosystem.
        return false;
    }
}
