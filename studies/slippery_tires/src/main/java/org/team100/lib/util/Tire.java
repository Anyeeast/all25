package org.team100.lib.util;

import org.team100.lib.dashboard.Glassy;
import org.team100.lib.geometry.Vector2d;
import org.team100.lib.logging.SupplierLogger2;
import org.team100.lib.logging.SupplierLogger2.DoubleSupplierLogger2;
import org.team100.lib.logging.SupplierLogger2.Vector2dLogger;
import org.team100.lib.telemetry.Telemetry.Level;

/**
 * Tire/carpet interaction model.
 * 
 * Imposes a maximum acceleration ("saturation") and slips a little below that,
 * proportional to acceleration.
 * 
 * Here's a link to the documentation
 * https://docs.google.com/document/d/1a-yI4T2AifRgIW3QJcWiV6-kvuVHfvHLoRkeGFjYLiI/edit
 */
public class Tire implements Glassy {
    // TODO: measure for different wheel/floors; Colson on tile will be much lower.
    private static final double kDefaultSaturationM_s_s = 10.0;
    private static final double kDefaultSlipAtSaturation0_1 = 0.1;


    private final double m_saturationM_s_s;
    private final double m_slipAtSaturation0_1;

    // LOGGING
    private final Vector2dLogger m_log_corner;
    private final Vector2dLogger m_log_wheel;
    private final DoubleSupplierLogger2 m_log_dtS;
    private final Vector2dLogger m_log_desired;
    private final Vector2dLogger m_log_limited;
    private final Vector2dLogger m_log_actual;
    private final Vector2dLogger m_log_limit;

    /** for testing */
    Tire(SupplierLogger2 parent, double saturationM_s_s, double slip0_1) {
        m_saturationM_s_s = saturationM_s_s;
        m_slipAtSaturation0_1 = slip0_1;
        SupplierLogger2 child = parent.child(this);
        m_log_corner = child.vector2dLogger(Level.TRACE, "corner M_s");
        m_log_wheel = child.vector2dLogger(Level.TRACE, "wheel M_s");
        m_log_dtS = child.doubleLogger(Level.COMP, "dtS");
        m_log_desired = child.vector2dLogger(Level.COMP, "desired accel M_s_s");
        m_log_limited = child.vector2dLogger(Level.COMP, "limited accel M_s_s");
        m_log_actual = child.vector2dLogger(Level.COMP, "actual M_s");
        m_log_limit = child.vector2dLogger(Level.COMP, "limit M_s_s");
    }

    public static Tire noslip(SupplierLogger2 parent) {
        return new Tire(parent, Double.MAX_VALUE, 0.0);
    }

    public static Tire defaultTire(SupplierLogger2 parent) {
        return new Tire(parent, kDefaultSaturationM_s_s, kDefaultSlipAtSaturation0_1);
    }

    /**
     * Actual corner velocity at the end of the current period after slip and
     * saturation are taken into account.
     * 
     * @param cornerM_s corner velocities at the start of the current period, i.e.
     *                  the previous step.
     * @param wheelM_s  wheel velocities using measurements from wheel sensors: the
     *                  position at the end of the current period minus the position
     *                  at the start.
     * @param dtS       length of the current period
     */
    public Vector2d actual(Vector2d cornerM_s, Vector2d wheelM_s, double dtS) {
        m_log_corner.log(() -> cornerM_s);
        m_log_wheel.log(() -> wheelM_s);
        m_log_dtS.log(() -> dtS); // usually about 0.02

        Vector2d desiredAccelM_s_s = desiredAccelM_s_s(cornerM_s, wheelM_s, dtS);
        m_log_desired.log(() -> desiredAccelM_s_s);
        double fraction = fraction(desiredAccelM_s_s);
        double scale = scale(fraction);
        Vector2d scaledAccelM_s_s = scaledAccelM_s_s(desiredAccelM_s_s, scale);
        Vector2d limitedAccelM_s_s = limit(scaledAccelM_s_s);
        m_log_limited.log(() -> limitedAccelM_s_s);

        // Vector2d actual = apply(cornerM_s, desiredAccelM_s_s, dtS);
        Vector2d actual = apply(cornerM_s, limitedAccelM_s_s, dtS);
        m_log_actual.log(() -> actual);
        return actual;
    }

    //////////////////////////////////

    Vector2d apply(Vector2d speedM_s, Vector2d accelM_s_s, double dtS) {
        return speedM_s.plus(accelM_s_s.times(dtS));
    }

    /**
     * Acceleration implied by the corner speeds (entering the current period) and
     * wheel speeds (exiting the current period).
     */
    Vector2d desiredAccelM_s_s(Vector2d cornerM_s, Vector2d wheelM_s, double dtS) {
        return wheelM_s.minus(cornerM_s).times(1 / dtS);
    }

    /**
     * Fraction of saturation accel.
     */
    double fraction(Vector2d desiredAccelM_s_s) {
        return desiredAccelM_s_s.norm() / m_saturationM_s_s;
    }

    /**
     * Resulting slip, expressed as a fraction of the desired acceleration.
     * 
     * Tires slip more when they are pushed harder:
     * 
     * <pre>
     * saturation fraction 0.0 => 100% of desired accel (no slip)
     * saturation fraction 0.5 =>  95% of desired accel
     * saturation fraction 1.0 =>  90% of desired accel (10% slip)
     * </pre>
     */
    double scale(double fraction) {
        double slip = m_slipAtSaturation0_1 * Math.min(1.0, Math.max(0.0, fraction));
        return 1 - slip;
    }

    Vector2d scaledAccelM_s_s(Vector2d desiredAccelM_s_s, double scale) {
        return desiredAccelM_s_s.times(scale);
    }

    /** The given acceleration, limited by saturation. */
    Vector2d limit(Vector2d scaledAccelM_s_s) {
        double normM_s_s = scaledAccelM_s_s.norm();
        double saturationM_s_s = m_saturationM_s_s;
        if (normM_s_s <= saturationM_s_s) {
            m_log_limit.log(() -> scaledAccelM_s_s);
            return scaledAccelM_s_s;
        }
        Vector2d limitedM_s_s = scaledAccelM_s_s.times(saturationM_s_s / normM_s_s);
        m_log_limit.log(() -> limitedM_s_s);
        return limitedM_s_s;
    }

    @Override
    public String getGlassName() {
        return "Tire";
    }
}
