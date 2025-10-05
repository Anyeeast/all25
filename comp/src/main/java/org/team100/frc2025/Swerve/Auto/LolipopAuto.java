package org.team100.frc2025.Swerve.Auto;

import static edu.wpi.first.wpilibj2.command.Commands.parallel;
import static edu.wpi.first.wpilibj2.command.Commands.runOnce;
import static edu.wpi.first.wpilibj2.command.Commands.sequence;
import static edu.wpi.first.wpilibj2.command.Commands.waitUntil;

import java.util.function.BiFunction;
import java.util.function.DoubleConsumer;

import org.team100.frc2025.CalgamesArm.CalgamesMech;
import org.team100.frc2025.grip.Manipulator;
import org.team100.lib.commands.Done;
import org.team100.lib.commands.drivetrain.DriveToPoseWithProfile;
import org.team100.lib.commands.drivetrain.DriveToPoseWithTrajectory;
import org.team100.lib.config.ElevatorUtil.ScoringLevel;
import org.team100.lib.controller.drivetrain.SwerveController;
import org.team100.lib.field.FieldConstants;
import org.team100.lib.field.FieldConstants.ReefPoint;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.drivetrain.state.SwerveModel;
import org.team100.lib.profile.HolonomicProfile;
import org.team100.lib.trajectory.Trajectory100;
import org.team100.lib.trajectory.TrajectoryPlanner;
import org.team100.lib.visualization.TrajectoryVisualization;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelRaceGroup;

public class LolipopAuto {
    private static final double HEED_RADIUS_M = 3;

    public static Command get(
            LoggerFactory logger,
            CalgamesMech mech,
            Manipulator manipulator,
            SwerveController controller,
            HolonomicProfile profile,
            SwerveDriveSubsystem drive,
            TrajectoryPlanner planner,
            DoubleConsumer heedRadiusM,
            SwerveKinodynamics kinodynamics,
            TrajectoryVisualization viz) {
        DriveToPoseWithTrajectory toReefTrajectory = new DriveToPoseWithTrajectory(
                () -> FieldConstants.makeGoal(ScoringLevel.L4, ReefPoint.A).plus(new Transform2d(-.5,.5,new Rotation2d())), drive,
                generateTrajectory(planner),
                controller, viz);

        DriveToPoseWithProfile toReefA = new DriveToPoseWithProfile(
            logger, drive, controller, profile,
            () -> FieldConstants.makeGoal(ScoringLevel.L4, ReefPoint.A));

        DriveToPoseWithProfile toCenterCoral = new DriveToPoseWithProfile(
                logger, drive, controller, profile,
                () -> new Pose2d(FieldConstants.CoralMark.CENTER.value, new Rotation2d())
                        .plus(new Transform2d(new Translation2d(0.7, 0), new Rotation2d())));

        DriveToPoseWithProfile toReefB = new DriveToPoseWithProfile(
                logger, drive, controller, profile,
                () -> FieldConstants.makeGoal(ScoringLevel.L4, ReefPoint.B));

        Done toL4 = mech.homeToL4();
        Done toL4second = mech.homeToL4();
        ParallelRaceGroup eject = manipulator.centerEject().withTimeout(0.5);
        ParallelRaceGroup ejectsecond = manipulator.centerEject().withTimeout(0.5);
        return sequence(
                toReefTrajectory.until(toReefTrajectory::isDone),
                parallel(
                        runOnce(() -> heedRadiusM.accept(HEED_RADIUS_M)),
                        toReefA,
                        mech.profileHomeAndThenRest().until(toReefA::isDone).andThen(toL4),
                        waitUntil(() -> toReefA.isDone() && toL4.isDone())
                                .andThen(eject))
                        .until(() -> (toReefA.isDone() && toL4.isDone() && eject.isFinished())),
                parallel(
                        toCenterCoral,
                        mech.pickWithProfile(),
                        manipulator.centerIntake()).until(manipulator::hasCoral).withTimeout(3).andThen(manipulator::stop),
                parallel(
                        toReefB,
                        mech.profileHomeAndThenRest().until(toReefB::isDone).andThen(toL4second),
                        waitUntil(() -> toReefB.isDone() && toL4second.isDone())
                                .andThen(ejectsecond))
                        .until(() -> (toReefB.isDone() && toL4second.isDone() && ejectsecond.isFinished())),
                mech.l4ToHome());
    }

    private static BiFunction<SwerveModel, Pose2d, Trajectory100> generateTrajectory(TrajectoryPlanner planner) {
        return (start, end) -> planner.movingToRest(start, new Rotation2d(Math.PI), 10, end,
                new Rotation2d(-Math.PI / 2), 10);
    }
}
