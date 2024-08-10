package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.exception.EvolutionException;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.MalformedInputException;

public class SpatialCoordinates {

    //TODO convert to records?
    public final int xAxis;
    public final int yAxis;
    public final int zAxis;

    public SpatialCoordinates(final int xAxis, final int yAxis, final int zAxis ){
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public String toString(){
        return String.format("(%d,%d,%d)", xAxis, yAxis, zAxis );
    }

    public static SpatialCoordinates fromString(final String coordinates){
       SpatialCoordinates retVal = null;
       if(StringUtils.isNotEmpty(coordinates)){
           if( '(' == coordinates.charAt(0) && ')' == coordinates.charAt(coordinates.length()-1)){
               final String unwrapped = coordinates.substring(1,coordinates.length()-1);
               final String[] segmentedCoords = StringUtils.splitPreserveAllTokens(unwrapped,',');
               if( 3 == segmentedCoords.length ){
                   retVal = new SpatialCoordinates( Integer.parseInt(segmentedCoords[0]),
                           Integer.parseInt(segmentedCoords[1]), Integer.parseInt(segmentedCoords[2]));
               } else {
                   throw new EvolutionException(coordinates + " is not a valid SpatialCoordinate.");
               }
           } else {
              throw new EvolutionException(coordinates + " is malformed.");
           }
       }

       return retVal;
    }
}
