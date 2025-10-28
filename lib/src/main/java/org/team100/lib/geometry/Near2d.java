package org.team100.lib.geometry;

import java.util.function.BiPredicate;

import edu.wpi.first.math.geometry.Translation2d;

/** True if the two translations are near each other. */
public class Near2d implements BiPredicate<Translation2d, Translation2d> {

    private final double m_threshold;

    public Near2d(double threshold) {
        m_threshold = threshold;
    }

    @Override
    public boolean test(Translation2d a, Translation2d b) {
        return a.getDistance(b) < m_threshold;
    }

}
