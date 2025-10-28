package org.team100.lib.geometry;

import java.util.Collection;
import java.util.function.Function;

import edu.wpi.first.math.geometry.Translation2d;

/** Yields the centroid of the translations. */
public class Centroid2d implements Function<Collection<Translation2d>, Translation2d> {
    @Override
    public Translation2d apply(Collection<Translation2d> c) {
        Translation2d sum = new Translation2d();
        int count = 0;
        for (Translation2d t : c) {
            sum = sum.plus(t);
            count++;
        }
        return sum.div(count);
    }
}