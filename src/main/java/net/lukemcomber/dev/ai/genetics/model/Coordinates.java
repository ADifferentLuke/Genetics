package net.lukemcomber.dev.ai.genetics.model;

public class Coordinates {

    public final int xAxis;
    public final int yAxis;
    public final int zAxis;

    public Coordinates( final int xAxis, final int yAxis, final int zAxis ){
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public String toString(){
        return String.format("(%d,%d,%d)", xAxis, yAxis, zAxis );
    }
}
