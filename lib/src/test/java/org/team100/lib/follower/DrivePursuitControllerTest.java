package org.team100.lib.follower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.team100.lib.geometry.GeometryUtil;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.TestLoggerFactory;
import org.team100.lib.logging.primitive.TestPrimitiveLogger;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamicsFactory;
import org.team100.lib.timing.TimedPose;
import org.team100.lib.timing.TimingConstraint;
import org.team100.lib.timing.TimingConstraintFactory;
import org.team100.lib.trajectory.Trajectory100;
import org.team100.lib.trajectory.TrajectoryPlanner;
import org.team100.lib.trajectory.TrajectoryTimeIterator;
import org.team100.lib.trajectory.TrajectoryTimeSampler;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

class DrivePursuitControllerTest {
    boolean dump = false;
    private static final double kDelta = 0.001;
    private static final LoggerFactory logger = new TestLoggerFactory(new TestPrimitiveLogger());
    private static final SwerveKinodynamics kSmoothKinematicLimits = SwerveKinodynamicsFactory.forTest3();

    @Test
    void testPursuit() {

        // first right and then ahead
        List<Pose2d> waypoints = List.of(
                new Pose2d(0, 0, Rotation2d.fromDegrees(270)),
                new Pose2d(10, -10, Rotation2d.fromDegrees(0)));
        // face +y and end up -x
        List<Rotation2d> headings = List.of(
                GeometryUtil.fromDegrees(90),
                GeometryUtil.fromDegrees(180));
        // so this trajectory is actually (robot-relative) -x the whole way, more or
        // less.

        List<TimingConstraint> constraints = new TimingConstraintFactory(kSmoothKinematicLimits).fast();

        Trajectory100 trajectory = TrajectoryPlanner.restToRest(waypoints, headings, constraints);

        // why is this so large?
        assertEquals(1300, trajectory.length());

        TrajectoryTimeSampler view = new TrajectoryTimeSampler(trajectory);

        TrajectoryTimeIterator iter = new TrajectoryTimeIterator(view);

        DrivePursuitFollower controller = new DrivePursuitFollower(logger, kSmoothKinematicLimits);
        controller.setTrajectory(iter);

        // this is a series of perfect trajectory following states,
        // based on the trajectory itself.

        {

            ChassisSpeeds output = controller.update(0,
                    new Pose2d(new Translation2d(0, 0), Rotation2d.fromRadians(1.57079632679)),
                    new ChassisSpeeds());
            // I turned on minimum speed so it's not zero.
            assertEquals(-1, output.vxMetersPerSecond, 0.05);
            assertEquals(0, output.vyMetersPerSecond, 0.05);
            // omega is NaN, i think pursuit ignores omega, it uses feedforward only.
            // assertEquals(0, output.omegaRadiansPerSecond, 0.001);
        }

        {

            Pose2d current_state = new Pose2d(new Translation2d(0.25, -3.5), Rotation2d.fromRadians(1.69));
            ChassisSpeeds output = controller.update(4.0,
                    current_state,
                    new ChassisSpeeds());
            // remember, facing +90, moving -90, so this should be like -1
            // turning slowly to the left
            // i think pure pursuit might ignore omega
            verify(-1.95, -0.15, 0, output);

            TimedPose path_setpoint = controller.getSetpoint(current_state).get();
            assertEquals(0.25, path_setpoint.state().getPose().getX(), 0.01);
            assertEquals(-3.5, path_setpoint.state().getPose().getY(), 0.05);
            assertEquals(1.69, path_setpoint.state().getHeading().getRadians(), 0.01);
            assertEquals(2.34, path_setpoint.getTimeS(), 0.05);
            assertEquals(1.96, path_setpoint.velocityM_S(), 0.01);
            // accel is back-emf limited here.
            assertEquals(-0.034, path_setpoint.acceleration(), 0.001);

            Twist2d errorTwist = DriveTrajectoryFollowerUtil.getErrorTwist(current_state, path_setpoint);
            assertEquals(0, errorTwist.dx, 0.05);
            assertEquals(0, errorTwist.dy, 0.05);
            assertEquals(0, errorTwist.dtheta, 0.05);
        }
        {

            Pose2d current_state = new Pose2d(new Translation2d(1.85, -7.11), Rotation2d.fromRadians(2.22));
            ChassisSpeeds output = controller.update(8.0,
                    current_state,
                    new ChassisSpeeds());
            // this is more Y than PID because it looks ahead
            verify(-1.90, -0.14, 0, output);

            TimedPose path_setpoint = controller.getSetpoint(current_state).get();
            assertEquals(1.85, path_setpoint.state().getPose().getX(), 0.05);
            assertEquals(-7.11, path_setpoint.state().getPose().getY(), 0.01);
            assertEquals(2.22, path_setpoint.state().getHeading().getRadians(), 0.01);
            assertEquals(4.41, path_setpoint.getTimeS(), 0.05);
            assertEquals(1.91, path_setpoint.velocityM_S(), 0.001);
            assertEquals(0.00, path_setpoint.acceleration(), 0.001);

            Twist2d errorTwist = DriveTrajectoryFollowerUtil.getErrorTwist(current_state, path_setpoint);
            assertEquals(0, errorTwist.dx, 0.05);
            assertEquals(0, errorTwist.dy, 0.01);
            assertEquals(0, errorTwist.dtheta, 0.01);
        }
    }

    @Test
    void testPreviewDt() {
        SwerveKinodynamics limits = SwerveKinodynamicsFactory.forTest3();
        Pose2d start = GeometryUtil.kPoseZero;
        Pose2d end = start.plus(new Transform2d(1, 0, GeometryUtil.kRotationZero));

        Translation2d currentTranslation = start.getTranslation();
        Translation2d goalTranslation = end.getTranslation();
        Translation2d translationToGoal = goalTranslation.minus(currentTranslation);
        Rotation2d angleToGoal = translationToGoal.getAngle();
        List<Pose2d> waypointsM = List.of(
                new Pose2d(currentTranslation, angleToGoal),
                new Pose2d(goalTranslation, angleToGoal));

        List<Rotation2d> headings = List.of(
                start.getRotation(),
                end.getRotation());

        List<TimingConstraint> constraints = new TimingConstraintFactory(limits).fast();

        Trajectory100 trajectory = TrajectoryPlanner.restToRest(waypointsM, headings, constraints);

        TrajectoryTimeSampler sampler = new TrajectoryTimeSampler(trajectory);

        TrajectoryTimeIterator iter = new TrajectoryTimeIterator(sampler);

        // iter is at zero so time is zero
        assertEquals(0, DrivePursuitFollower.previewDt(iter,
                new Pose2d(0, 0, GeometryUtil.kRotationZero)).getAsDouble(), kDelta);
        // 0.828 is 1 second along the trajectory
        assertEquals(1, DrivePursuitFollower.previewDt(iter,
                new Pose2d(0.828, 0, GeometryUtil.kRotationZero)).getAsDouble(),
                kDelta);
        // the whole trajectory takes 1.414 seconds, but the
        // preview finds the "off the end" time instead.
        // this seems like a bug.
        assertEquals(2, DrivePursuitFollower.previewDt(iter,
                new Pose2d(1, 0, GeometryUtil.kRotationZero)).getAsDouble(), kDelta);

    }

    @Test
    void testNearPreviewDt() {
        SwerveKinodynamics limits = SwerveKinodynamicsFactory.forTest3();
        Pose2d start = GeometryUtil.kPoseZero;
        Pose2d end = start.plus(new Transform2d(1, 0, GeometryUtil.kRotationZero));

        Translation2d currentTranslation = start.getTranslation();
        Translation2d goalTranslation = end.getTranslation();
        Translation2d translationToGoal = goalTranslation.minus(currentTranslation);
        Rotation2d angleToGoal = translationToGoal.getAngle();
        List<Pose2d> waypointsM = List.of(
                new Pose2d(currentTranslation, angleToGoal),
                new Pose2d(goalTranslation, angleToGoal));

        List<Rotation2d> headings = List.of(
                start.getRotation(),
                end.getRotation());

        List<TimingConstraint> constraints = new TimingConstraintFactory(limits).fast();

        Trajectory100 trajectory = TrajectoryPlanner.restToRest(waypointsM, headings, constraints);

        TrajectoryTimeSampler sampler = new TrajectoryTimeSampler(trajectory);

        TrajectoryTimeIterator iter = new TrajectoryTimeIterator(sampler);

        // for a pose that isn't on the trajectory at all, it picks the nearest point
        assertEquals(0, DrivePursuitFollower.previewDt(iter,
                new Pose2d(0, 1, GeometryUtil.kRotationZero)).getAsDouble(), kDelta);
        assertEquals(1, DrivePursuitFollower.previewDt(iter,
                new Pose2d(0.828, 1, GeometryUtil.kRotationZero)).getAsDouble(), kDelta);
        assertEquals(2, DrivePursuitFollower.previewDt(iter,
                new Pose2d(1, 1, GeometryUtil.kRotation90)).getAsDouble(), kDelta);
    }

    void verify(double vx, double vy, double omega, ChassisSpeeds output) {
        assertEquals(vx, output.vxMetersPerSecond, 0.05);
        assertEquals(vy, output.vyMetersPerSecond, 0.05);
        assertEquals(omega, output.omegaRadiansPerSecond, 0.05);
    }

}
