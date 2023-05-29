package net.lukemcomber.dev.ai.genetics.world.terrain;


/**
 * A common interface for setting, tracking, and manipulating terrain property
 * @param <T>
 */
public interface TerrainProperty<T> {

   /**
    * @return Get the value of the property
    */
   T getValue( );

   /**
    * Sets the value of the terrain property
    * @param t the value to set
    */
   void setValue( T t );

   void setValue(final String s );

   /**
    * Returns the unique, deterministic string representation of the genome
    * @return unique id
    */
   String getId();

   /**
    * Clones a new Terrain Property with all the values of the original
    * @return new copy of this
    */
   TerrainProperty<T> clone();

}
