package org.team100.alliance;

import org.team100.commands.SourceDefault;
import org.team100.control.SelectorPilot;
import org.team100.lib.camera.NoteSighting;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.pilot.Auton;
import org.team100.lib.pilot.Defender;
import org.team100.lib.pilot.Passer;
import org.team100.lib.pilot.Scorer;
import org.team100.lib.pilot.ShootPreload;
import org.team100.lib.planner.ForceViz;
import org.team100.robot.RobotAssembly;
import org.team100.robot.Source;
import org.team100.sim.Foe;
import org.team100.sim.SimWorld;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * This is modeled after 2024 Newton, which usually ran one passer, one scorer,
 * and one defender (though not in Einstein, since Daly subverted that
 * strategy). In auton, the scorer takes the near 3 and 2 of the far notes,
 * the passer takes the other 3 far notes, and defender stays still. The actual
 * auton strategies were defensive, disrupting the center, but the strategies
 * here just focus on offense.
 */
public class Red implements Alliance {
    private final RobotAssembly scorer;
    private final RobotAssembly passer;
    private final RobotAssembly defender;
    private final Source source;

    public Red(SimWorld world, ForceViz viz, SwerveKinodynamics swerveKinodynamics) {
        // near 3
        scorer = new RobotAssembly(
                swerveKinodynamics,
                x -> SelectorPilot.autonSelector(
                        new Auton(x.getDrive()::getPose,
                                () -> x.getCamera().recentNoteSightings().values().stream().map(NoteSighting::position)
                                        .toList(),
                                x.getIndexer()::full,
                                new Pose2d(14, 7, new Rotation2d(-0.5)), false,
                                11, 10, 9),
                        new Scorer(
                                x.getDrive()::getPose,
                                x.getCamera()::noteNearby,
                                x.getIndexer()::full,
                                () -> world.getScorekeeper().redAmplified() > 0,
                                new Pose2d(13.5, 5.5, new Rotation2d()),
                                new Pose2d(14, 7, new Rotation2d()))),
                new Foe("red scorer", world, 0, false),
                viz,
                false);
        // initially in the upper corner
        scorer.setState(15.3, 7, 0, 0, 0);

        // lower far 3
        passer = new RobotAssembly(
                swerveKinodynamics,
                x -> SelectorPilot.autonSelector(
                        new Auton(
                                x.getDrive()::getPose,
                                () -> x.getCamera().recentNoteSightings().values().stream().map(NoteSighting::position)
                                        .toList(),
                                x.getIndexer()::full,
                                new Pose2d(13.5, 3.4, new Rotation2d(0.5)), false,
                                4, 5, 6),
                        new Passer(
                                x.getDrive()::getPose,
                                x.getCamera()::noteNearby,
                                x.getIndexer()::full)),
                new Foe("red passer", world, 0, false),
                viz,
                false);
        // initially below the subwoofer
        passer.setState(15.3, 3, 0, 0, 0);

        // do nothing
        defender = new RobotAssembly(
                swerveKinodynamics,
                x -> SelectorPilot.autonSelector(
                        new ShootPreload(x.getDrive()::getPose),
                        new Defender()),
                new Foe("red defender", world, 0, false),
                viz,
                false);
        // initially near subwoofer
        defender.setState(15.8, 4.3, Math.PI / 3, 0, 0);

        source = new Source(world, new Translation2d(1.0, 1.0));
        source.setDefaultCommand(new SourceDefault(source, world, false, false));
    }

    @Override
    public void reset() {
        scorer.reset();
        passer.reset();
        defender.reset();
    }

    @Override
    public void begin() {
        scorer.begin();
        passer.begin();
        defender.begin();
    }

    @Override
    public void periodic() {
        scorer.periodic();
        passer.periodic();
        defender.periodic();
    }
}
