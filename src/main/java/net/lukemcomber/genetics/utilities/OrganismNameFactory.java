package net.lukemcomber.genetics.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class OrganismNameFactory {

    private static final AtomicInteger autoIncrementingId = new AtomicInteger(0);

    public static String nextName(){
        return "org-%d".formatted( autoIncrementingId.getAndIncrement());
    }

    public static void reset(){
        autoIncrementingId.set(0);
    }
}
