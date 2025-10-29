package org.team100.ballerina;

import java.util.function.Supplier;

import org.team100.lib.coherence.Cache;
import org.team100.lib.coherence.ObjectCache;
import org.team100.lib.framework.TimedRobot100;
import org.team100.lib.geometry.GlobalVelocityR3;
import org.team100.lib.hid.Velocity;
import org.team100.lib.logging.Level;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.LoggerFactory.DoubleArrayLogger;
import org.team100.lib.state.ModelR3;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

/**
 * Provide a pose estimate with manual control.
 * 
 * This is just for testing, a way to "drive around" in simulation without using
 * a full simulated drivetrain.
 * 
 * It provides Field2d visualization of the robot using the name "robot".
 */
public class ManualPose {
    private static final double DT = TimedRobot100.LOOP_PERIOD_S;
    private static final double MAX_V = 1.0;
    private static final double MAX_OMEGA = 1.0;
    private final DoubleArrayLogger m_log_field_robot;
    private final Supplier<Velocity> m_v;
    private final ObjectCache<ModelR3> m_stateCache;
    /** Used only by update(). */
    private ModelR3 m_state;

    public ManualPose(
            LoggerFactory fieldLogger,
            Supplier<Velocity> v,
            Pose2d initial) {
        m_log_field_robot = fieldLogger.doubleArrayLogger(Level.COMP, "robot");
        m_v = v;
        m_state = new ModelR3(initial);
        m_stateCache = Cache.of(this::update);
    }

    public ModelR3 getState() {
        return m_stateCache.get();
    }

    public Pose2d getPose() {
        return getState().pose();
    }

    public void periodic() {
        m_log_field_robot.log(this::poseArray);
    }

    private double[] poseArray() {
        Pose2d pose = getPose();
        return new double[] {
                pose.getX(),
                pose.getY(),
                pose.getRotation().getDegrees() };
    }

    private ModelR3 update() {
        Velocity v = m_v.get();
        double vx = v.x() * MAX_V;
        double vy = v.y() * MAX_V;
        double omega = v.theta() * MAX_OMEGA;
        m_state = new ModelR3(
                new Pose2d(
                        m_state.pose().getX() + vx * DT,
                        m_state.pose().getY() + vy * DT,
                        m_state.pose().getRotation().plus(new Rotation2d(omega * DT))),
                new GlobalVelocityR3(vx, vy, omega));
        return m_state;
    }
}
