package org.team100.lib.planner;

import java.util.function.UnaryOperator;

import org.team100.lib.geometry.GeometryUtil;
import org.team100.lib.motion.drivetrain.DriveSubsystemInterface;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeDelta;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeVelocity;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public class DriveUtil {
    private static final int kAngularP = 5;
    private static final int kCartesianP = 5;

    private final SwerveKinodynamics m_swerveKinodynamics;
    private final DriveSubsystemInterface m_drive;
    private final ForceViz m_viz;
    private final UnaryOperator<FieldRelativeVelocity> m_tactics;
    private final boolean m_debug;

    public DriveUtil(
            SwerveKinodynamics swerveKinodynamics,
            DriveSubsystemInterface drive,
            ForceViz viz,
            UnaryOperator<FieldRelativeVelocity> tactics,
            boolean debug) {
        m_swerveKinodynamics = swerveKinodynamics;
        m_drive = drive;
        m_viz = viz;
        m_tactics = tactics;
        m_debug = debug;
    }

    /** Proportional feedback with a limiter. */
    public FieldRelativeVelocity goToGoal(Pose2d pose, Pose2d m_goal) {
        if (m_debug)
            System.out.printf(" pose (%5.2f, %5.2f) target (%5.2f, %5.2f)",
                    pose.getX(), pose.getY(), m_goal.getX(), m_goal.getY());
        FieldRelativeDelta positionError = FieldRelativeDelta.delta(pose, m_goal);
        double rotationError = MathUtil.angleModulus(positionError.getRotation().getRadians());
        FieldRelativeDelta cartesianU_FB = positionError.times(kCartesianP);
        double angularU_FB = rotationError * kAngularP;
        return new FieldRelativeVelocity(cartesianU_FB.getX(), cartesianU_FB.getY(), angularU_FB)
                .clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
    }

    public FieldRelativeVelocity goToGoalAligned(
            double angleToleranceRad,
            Pose2d pose,
            Translation2d targetFieldRelative) {

        if (m_debug)
            System.out.printf(" pose (%5.2f, %5.2f) target (%5.2f, %5.2f)",
                    pose.getX(), pose.getY(),
                    targetFieldRelative.getX(), targetFieldRelative.getY());

        Translation2d robotToTargetFieldRelative = targetFieldRelative.minus(pose.getTranslation());
        Rotation2d robotToTargetAngleFieldRelative = robotToTargetFieldRelative.getAngle();
        // intake is on the back
        Rotation2d intakeAngleFieldRelative = GeometryUtil.flip(pose.getRotation());
        double angleError = MathUtil.angleModulus(
                robotToTargetAngleFieldRelative.minus(intakeAngleFieldRelative).getRadians());

        boolean aligned = Math.abs(angleError) < angleToleranceRad;

        Translation2d cartesianU_FB = getCartesianError(
                robotToTargetFieldRelative,
                aligned,
                m_debug).times(kCartesianP);

        double angleU_FB = angleError * kAngularP;

        // we also want to turn the intake towards the note
        FieldRelativeVelocity desired = new FieldRelativeVelocity(cartesianU_FB.getX(), cartesianU_FB.getY(), angleU_FB)
                .clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
        if (!aligned) {
            // need to turn? avoid the edges.
            return finish(desired);
        }
        if (robotToTargetFieldRelative.getNorm() < 1) {
            // if close and aligned, don't need tactics at all.
            return desired;
        }
        return finish(desired);
    }

    /**
     * apply tactics based on the desired velocity, and then add those tactics to
     * the velocity, and return it.
     */
    public FieldRelativeVelocity finish(FieldRelativeVelocity desired) {
        if (m_debug)
            m_viz.desired(m_drive.getPose().getTranslation(), desired);
        if (m_debug)
            System.out.printf(" desire %s", desired);
        FieldRelativeVelocity v = m_tactics.apply(desired);
        v = v.plus(desired);
        v = v.clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
        if (m_debug)
            System.out.printf(" final %s\n", v);
        return v;
    }

    // this was originally in IndexerSubsystem because it's about the alignment
    // tolerance
    // of the indexer, but i (maybe temporarily?) put it here in order to separate
    // it from the simulator ("RobotBody" etc) stuff
    public static Translation2d getCartesianError(
            Translation2d robotToTargetFieldRelative,
            boolean aligned,
            boolean debug) {
        // Go this far from the note until rotated correctly.
        final double kPickRadius = 1;
        // Correct center-to-center distance for picking.
        final double kMinPickDistanceM = 0.437;
        double distance = robotToTargetFieldRelative.getNorm();
        if (distance < kMinPickDistanceM || !aligned) {
            // target distance is lower than the tangent point: we ran the note
            // over without picking it, so back up.
            // also back up if not aligned.
            if (debug)
                System.out.print(" unaligned");
            double targetDistance = distance - kPickRadius;
            return robotToTargetFieldRelative.times(targetDistance);
        }
        if (debug)
            System.out.print(" aligned");

        // aligned, drive over the note
        return robotToTargetFieldRelative;
    }

}
