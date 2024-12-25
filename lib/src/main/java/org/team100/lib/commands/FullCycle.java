package org.team100.lib.commands;

import java.util.List;

import org.team100.lib.commands.drivetrain.DriveToWaypoint3;
import org.team100.lib.controller.drivetrain.HolonomicFieldRelativeController;
import org.team100.lib.dashboard.Glassy;
import org.team100.lib.geometry.GeometryUtil;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.timing.ConstantConstraint;
import org.team100.lib.trajectory.StraightLineTrajectory;
import org.team100.lib.trajectory.TrajectoryMaker;
import org.team100.lib.visualization.TrajectoryVisualization;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

/**
 * An example of a "full auto" strategy, running a sequence of commands. Put
 * this inside a RepeatCommand to run it continuously.
 */
public class FullCycle extends SequentialCommandGroup implements Glassy {
    private static final double maxVelocityM_S = 2.0;
    private static final double maxAccelM_S_S = 2;
    private static final Pose2d waypoint0 = new Pose2d(6, 2, GeometryUtil.kRotationZero);
    private static final Pose2d waypoint1 = new Pose2d(2, 2, GeometryUtil.kRotationZero);

    public FullCycle(
            LoggerFactory parent,
            SwerveDriveSubsystem drivetrain,
            HolonomicFieldRelativeController controller,
            TrajectoryVisualization viz) {
        DriveToWaypoint3.Log log = new DriveToWaypoint3.Log(parent.child(this));
        TrajectoryMaker tmaker = new TrajectoryMaker(List.of(new ConstantConstraint(maxVelocityM_S, maxAccelM_S_S)));
        StraightLineTrajectory maker = new StraightLineTrajectory(true, tmaker);
        // for now just drive back and forth.
        addCommands(
                new DriveToWaypoint3(
                        log, waypoint0, drivetrain, maker, controller, viz),
                new DriveToWaypoint3(
                        log, waypoint1, drivetrain, maker, controller, viz));
    }

}
