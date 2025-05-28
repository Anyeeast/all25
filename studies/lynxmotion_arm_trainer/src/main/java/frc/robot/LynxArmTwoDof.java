package frc.robot;

import org.team100.lib.motion.lynxmotion_arm.LynxArmConfig;
import org.team100.lib.motion.lynxmotion_arm.LynxArmKinematics;
import org.team100.lib.motion.lynxmotion_arm.AnalyticLynxArmKinematics;
import org.team100.lib.motion.lynxmotion_arm.LynxArmPose;
import org.team100.lib.motion.lynxmotion_arm.TwoDofArmConfig;
import org.team100.lib.motion.lynxmotion_arm.TwoDofArmPosition;
import org.team100.lib.motion.lynxmotion_arm.TwoDofKinematics;
import org.team100.lib.motion.lynxmotion_arm.AnalyticTwoDofKinematics;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Uses only two degrees of freedom of the Lynxmotion arm.
 * 
 * This is intended for learning about the two-DOF model.
 */
public class LynxArmTwoDof extends SubsystemBase implements AutoCloseable {
    private static final Translation2d HOME = new Translation2d(0.2, 0.2);

    private final CalibratedServo m_swing;
    private final CalibratedServo m_boom;
    private final CalibratedServo m_stick;
    private final CalibratedServo m_wrist;
    private final CalibratedServo m_twist;
    private final CalibratedServo m_grip;

    private final TwoDofKinematics m_kinematics;
    private final LynxArmKinematics m_fullKinematics;

    public LynxArmTwoDof() {
        // yaw; joint zero is is in the middle of the servo range; unconstrained.
        m_swing = new CalibratedServo(0,
                new Clamp(-Math.PI, Math.PI),
                new AffineFunction(-3.216, 1.534));

        // these are placeholders
        // TODO: calibrate all the axes.

        // pitch; joint zero and servo zero are aligned; unconstrained.
        m_boom = new CalibratedServo(1,
                new Clamp(-Math.PI, Math.PI),
                new AffineFunction(-Math.PI, 0));

        // pitch; joint zero is servo max; constrained in the positive direction.
        m_stick = new CalibratedServo(2,
                new Clamp(0, Math.PI),
                new AffineFunction(-Math.PI, Math.PI));

        // pitch; joint zero is in the middle of the servo range; unconstrained
        m_wrist = new CalibratedServo(3,
                new Clamp(-Math.PI, Math.PI),
                new AffineFunction(-Math.PI, Math.PI / 2));

        // roll; joint zero is in the middle of the servo range; unconstrained.
        m_twist = new CalibratedServo(4,
                new Clamp(-Math.PI, Math.PI),
                new AffineFunction(-Math.PI, Math.PI / 2));

        // the grip axis measures the width of the jaws.
        // TODO: calibrate in meters
        m_grip = new CalibratedServo(5,
                new Clamp(0, 0.02),
                new AffineFunction(-0.02, 0.02));

        // these are constant
        m_swing.set(0);
        m_wrist.set(0);
        m_twist.set(0);

        m_kinematics = new AnalyticTwoDofKinematics(0.146, 0.298);
        m_fullKinematics = AnalyticLynxArmKinematics.real();

        setPosition(HOME);
    }

    /** Translation coordinates are x-forward y-up, measured from the boom joint. */
    public void setPosition(Translation2d end) {
        TwoDofArmConfig q = m_kinematics.inverse(end);
        // the joint coordinates use the 3d convention which is inverted
        // from the 2d one, so fix it here.
        m_boom.set(-1.0 * q.q1());
        m_stick.set(-1.0 * q.q2());
    }

    public TwoDofArmPosition getPosition() {
        TwoDofArmConfig q = getMeasuredConfig();
        return m_kinematics.forward(q);
    }

    public TwoDofArmConfig getMeasuredConfig() {
        // invert the actual angles to match the 2dof convention
        return new TwoDofArmConfig(
                -1.0 * m_boom.get(),
                -1.0 * m_stick.get());
    }

    /** For visualization. */
    public LynxArmPose getPose() {
        LynxArmConfig q = new LynxArmConfig(
                m_swing.get(),
                m_boom.get(),
                m_stick.get(),
                m_wrist.get(),
                m_twist.get());
        return m_fullKinematics.forward(q);
    }

    /** x fwd, y up */
    public MoveCommandTwoDof moveTo(Translation2d goal) {
        return new MoveCommandTwoDof(this, goal);
    }

    public MoveCommandTwoDof moveHome() {
        return new MoveCommandTwoDof(this, HOME);
    }

    @Override
    public void close() {
        m_swing.close();
        m_boom.close();
        m_stick.close();
        m_wrist.close();
        m_twist.close();
        m_grip.close();
    }
}
