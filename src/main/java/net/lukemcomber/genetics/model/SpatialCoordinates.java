package net.lukemcomber.genetics.model;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

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
}
