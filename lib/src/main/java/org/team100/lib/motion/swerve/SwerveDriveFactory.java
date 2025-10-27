package org.team100.lib.motion.swerve;

import org.team100.lib.localization.AprilTagRobotLocalizer;
import org.team100.lib.localization.FreshSwerveEstimate;
import org.team100.lib.localization.OdometryUpdater;
import org.team100.lib.localization.SwerveHistory;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.motion.swerve.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.swerve.kinodynamics.limiter.SwerveLimiter;
import org.team100.lib.motion.swerve.module.SwerveModuleCollection;

import edu.wpi.first.wpilibj.RobotController;

/**
 * Pull together some of the drivetrain's dependencies so they don't pollute
 * Robot.java.
 */
public class SwerveDriveFactory {

    public static SwerveDriveSubsystem get(
            LoggerFactory fieldLogger,
            LoggerFactory driveLog,
            SwerveKinodynamics swerveKinodynamics,
            AprilTagRobotLocalizer localizer,
            OdometryUpdater odometryUpdater,
            SwerveHistory history,
            SwerveModuleCollection modules) {

        FreshSwerveEstimate estimate = new FreshSwerveEstimate(
                localizer,
                odometryUpdater,
                history);
        SwerveLocal swerveLocal = new SwerveLocal(
                driveLog,
                swerveKinodynamics,
                modules);
        SwerveLimiter limiter = new SwerveLimiter(
                driveLog,
                swerveKinodynamics,
                RobotController::getBatteryVoltage);
        return new SwerveDriveSubsystem(
                fieldLogger,
                driveLog,
                odometryUpdater,
                estimate,
                swerveLocal,
                limiter);
    }

}
