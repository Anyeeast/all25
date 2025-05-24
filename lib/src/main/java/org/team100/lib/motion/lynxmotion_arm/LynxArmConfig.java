package org.team100.lib.motion.lynxmotion_arm;

import java.util.OptionalDouble;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;

/**
 * Each joint is relative to its parent link, in radians.
 * 
 * The grip is not included here, since it isn't really an aspect of kinematics
 * at all.
 * 
 * maybe these should be "rotations" instead of doubles?
 * 
 * swing is optional because it can be indeterminate, for end positions
 * *on* the swing axis.
 */
public record LynxArmConfig(
        OptionalDouble swing,
        double boom,
        double stick,
        double wrist,
        OptionalDouble twist) {

    private static final boolean DEBUG = false;

    public LynxArmConfig(double swing, double boom, double stick, double wrist, double twist) {
        this(OptionalDouble.of(swing), boom, stick, wrist, OptionalDouble.of(twist));
    }

    public Transform3d swingT() {
        if (swing.isEmpty()) {
            if (DEBUG)
                System.out.println("empty swing");
            return Transform3d.kZero;
        }
        return yaw(swing.getAsDouble());
    }

    public Transform3d boomT() {
        return pitch(boom);
    }

    public Transform3d stickT() {
        return pitch(stick);
    }

    public Transform3d wristT() {
        return pitch(wrist);
    }

    public Transform3d twistT() {
        if (twist.isEmpty()) {
            if (DEBUG)
                System.out.println("empty twist");
            return Transform3d.kZero;
        }
        return roll(twist.getAsDouble());
    }

    static Transform3d roll(double x) {
        return new Transform3d(0, 0, 0, new Rotation3d(x, 0, 0));
    }

    static Transform3d pitch(double x) {
        return new Transform3d(0, 0, 0, new Rotation3d(0, x, 0));
    }

    static Transform3d yaw(double x) {
        return new Transform3d(0, 0, 0, new Rotation3d(0, 0, x));
    }
}