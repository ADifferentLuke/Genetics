package net.lukemcomber.genetics.universes;

import net.lukemcomber.genetics.exception.EvolutionException;
import net.lukemcomber.genetics.model.UniverseConstants;

public class PreCannedUniverses {

    public static UniverseConstants get( final String type ){
        final UniverseConstants retVal;
        switch (type){
            case FlatFloraUniverse.ID -> {
               retVal = new FlatFloraUniverse();
            }
            default ->
                throw new EvolutionException( "Unknown universe " + type );
        }
        return retVal;
    }
}
