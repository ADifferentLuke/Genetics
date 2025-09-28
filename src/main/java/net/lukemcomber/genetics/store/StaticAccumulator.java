package net.lukemcomber.genetics.store;

import java.awt.image.PackedColorModel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class StaticAccumulator {

    private final ConcurrentHashMap<String, AtomicLong> accumulators;

    private static final StaticAccumulator instance = new StaticAccumulator();

    private StaticAccumulator() {
        accumulators = new ConcurrentHashMap<>();
        dump();
    }

    public static void increment(final String counter) {
        synchronized (instance) {
            instance.accumulators.computeIfAbsent(counter, i -> new AtomicLong(0)).incrementAndGet();
        }
    }

    public static long get(final String counter) {
        synchronized (instance) {
            return instance.accumulators.computeIfAbsent(counter, i -> new AtomicLong(0)).get();
        }
    }

    private void dump() {

        final Thread thread = new Thread(() -> {
            final Thread myThread = Thread.currentThread();
            while (!myThread.isInterrupted()) {
                System.out.println("Exhausting .............. ");
                synchronized (instance) {
                    for (final String key : accumulators.keySet()) {
                        final AtomicLong al = accumulators.get(key);
                        System.out.println("\t" + key + " - " + al.get());
                        al.set(0);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
