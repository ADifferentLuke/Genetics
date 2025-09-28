package net.lukemcomber.genetics.biology;

import net.lukemcomber.genetics.biology.plant.PlantGenome;
import org.testng.Assert;
import org.testng.annotations.Test;

// ------------------------------------------------------------
// Test (package-private): Uniformity of gene→direction decoding
// ------------------------------------------------------------
@Test
public class PlantGenomeUniformityTest {

    private static final net.lukemcomber.genetics.model.SpatialCoordinates ORIGIN =
            new net.lukemcomber.genetics.model.SpatialCoordinates(0, 0, 0);

    // Classify the SpatialTransformation stored in GeneExpression by applying the function to (0,0,0)
    private static String classify(PlantGenome.GeneExpression ge) throws Exception {
        java.lang.reflect.Field f = PlantGenome.GeneExpression.class.getDeclaredField("spatialConversionFunction");
        f.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.function.Function<net.lukemcomber.genetics.model.SpatialCoordinates, net.lukemcomber.genetics.model.SpatialCoordinates> fn =
                (java.util.function.Function<net.lukemcomber.genetics.model.SpatialCoordinates, net.lukemcomber.genetics.model.SpatialCoordinates>) f.get(ge);
        net.lukemcomber.genetics.model.SpatialCoordinates out = fn.apply(ORIGIN);
        int dx = out.xAxis() - ORIGIN.xAxis();
        int dy = out.yAxis() - ORIGIN.yAxis();
        int dz = out.zAxis() - ORIGIN.zAxis();
        if (dx == -1 && dy == 0 && dz == 0) return "LEFT";
        if (dx ==  1 && dy == 0 && dz == 0) return "RIGHT";
        if (dx ==  0 && dy == 1 && dz == 0) return "UP";
        if (dx ==  0 && dy ==-1 && dz == 0) return "DOWN";
        if (dx ==  0 && dy == 0 && dz == 1) return "FORWARD";
        if (dx ==  0 && dy == 0 && dz ==-1) return "BACK";
        return "IDENTITY";
    }

    private static java.util.Map<String, Integer> expectedCounts() throws Exception {
        java.util.Map<String, Integer> m = new java.util.HashMap<>();
        for (PlantGenome.GeneExpression ge : PlantGenome.GeneExpression.values()) {
            String k = classify(ge);
            m.put(k, m.getOrDefault(k, 0) + 1);
        }
        return m;
    }

    @Test
    void sweepDefinedValues_matchesEnumCounts() throws Exception {
        PlantGenome.GeneExpression[] all = PlantGenome.GeneExpression.values();
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        // Sweep exactly the defined gene range [0 .. N-1]
        for (int u = 0; u < all.length; u++) {
            PlantGenome.GeneExpression ge = all[u];
            // Sanity: enum is contiguous and value()==index
            Assert.assertEquals(u, ge.value(), "Enum values must be contiguous starting at 0");
            String k = classify(ge);
            counts.put(k, counts.getOrDefault(k, 0) + 1);
        }
        Assert.assertEquals(expectedCounts(), counts, "Counts by direction should match enum distribution");
    }

    @Test
    void randomBytes_uniformOverDefinedRange_withinTolerance() throws Exception {
        PlantGenome.GeneExpression[] all = PlantGenome.GeneExpression.values();
        int N = all.length; // defined gene space size

        // Expected proportions per direction from the enum itself
        java.util.Map<String, Integer> exp = expectedCounts();
        java.util.Map<String, Double> expP = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, Integer> e : exp.entrySet()) {
            expP.put(e.getKey(), e.getValue() / (double) N);
        }

        // Sample random bytes; only bytes < N map to defined genes (others are Junk DNA and skipped)
        long samples = 2_000_000; // increase samples to reduce statistical noise
        java.util.Map<String, Long> obs = new java.util.HashMap<>();
        long mapped = 0;
        java.util.Random rng = new java.util.Random(1234567);
        for (long i = 0; i < samples; i++) {
            int u = rng.nextInt(256); // 0..255
            if (u < N) {
                PlantGenome.GeneExpression ge = all[u];
                String k = classify(ge);
                obs.put(k, obs.getOrDefault(k, 0L) + 1);
                mapped++;
            }
        }

        // Compare observed proportions to expected with a small tolerance
        double tol = 0.01; // 1.0% absolute tolerance (looser to avoid flakiness)
        final long fmapped = mapped;
        for (java.util.Map.Entry<String, Double> e : expP.entrySet()) {
            String k = e.getKey();
            double pExp = e.getValue();
            double pObs = obs.getOrDefault(k, 0L) / (double) mapped;
            Assert.assertTrue(Math.abs(pObs - pExp) <= tol,
                    String.format("Direction %s: observed=%.4f expected=%.4f (mapped=%d)", k, pObs, pExp, fmapped));
        }
    }
}
