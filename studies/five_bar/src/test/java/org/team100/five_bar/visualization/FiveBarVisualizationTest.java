package org.team100.five_bar.visualization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.team100.lib.motion.five_bar.FiveBarKinematics;
import org.team100.lib.motion.five_bar.JointPositions;
import org.team100.lib.motion.five_bar.Point;
import org.team100.lib.motion.five_bar.Scenario;

public class FiveBarVisualizationTest {
    private static final double kDelta = 0.001;

    private Scenario regularPentagon() {
        Scenario s = new Scenario();
        // unit side length
        // all sides the same
        s.a1 = 1.0;
        s.a2 = 1.0;
        s.a3 = 1.0;
        s.a4 = 1.0;
        s.a5 = 1.0;
        return s;
    }

    @Test
    void testPentagon() {
        Scenario s = regularPentagon();
        double t1 = 1.25664;
        double t5 = 1.88496;
        JointPositions j = FiveBarKinematics.forward(s, t1, t5);
        List<Point> p = FiveBarVisualization.links(j);
        // sides are all equal
        assertEquals(1.0, p.get(0).norm(), kDelta);
        assertEquals(1.0, p.get(1).norm(), kDelta);
        assertEquals(1.0, p.get(2).norm(), kDelta);
        assertEquals(1.0, p.get(3).norm(), kDelta);
        // parent-relative angles are all equal
        assertEquals(1.257, p.get(0).angle().orElseThrow(), kDelta);
        assertEquals(1.257, p.get(1).angle().orElseThrow(), kDelta);
        assertEquals(1.257, p.get(2).angle().orElseThrow(), kDelta);
        assertEquals(1.257, p.get(3).angle().orElseThrow(), kDelta);
    }
}
