package org.team100.alliance;

import org.team100.commands.SourceDefault;
import org.team100.control.ManualPilot;
import org.team100.control.SelectorPilot;
import org.team100.lib.camera.NoteSighting;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.pilot.AmpCycler;
import org.team100.lib.pilot.Auton;
import org.team100.lib.pilot.Defender;
import org.team100.lib.pilot.ShootPreload;
import org.team100.lib.pilot.SpeakerCycler;
import org.team100.lib.planner.ForceViz;
import org.team100.robot.RobotAssembly;
import org.team100.robot.Source;
import org.team100.sim.Friend;
import org.team100.sim.Player;
import org.team100.sim.SimWorld;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * This is modeled after 2024 Daly, which usually ran two cyclers and one
 * defender. In auton, one of the cyclers takes the 3 far notes, the defender
 * takes the 3 near ones, and the other cycler stays out of the way -- trying to
 * disrupt in reality, but just idle here.
 */
public class Blue implements Alliance {
    /**
     * Use a real player robot instead of an NPC.
     */
    private static final boolean kRealPlayer = true;
    private final RobotAssembly player;
    private final RobotAssembly friend1;
    private final RobotAssembly friend2;
    private final Source source;

    public Blue(SimWorld world, ForceViz viz, SwerveKinodynamics swerveKinodynamics) {
        // upper far 3
        if (kRealPlayer) {
            player = new RobotAssembly(
                    swerveKinodynamics,
                    x -> SelectorPilot.autonSelector(
                            new Auton(x.getDrive()::getPose,
                                    () -> x.getCamera().recentNoteSightings().values().stream()
                                            .map(NoteSighting::position)
                                            .toList(),
                                    x.getIndexer()::full,
                                    new Pose2d(3.0, 7.5, new Rotation2d(-2.75)), false,
                                    8, 7, 6),
                            new ManualPilot(x.getDrive())),
                    new Player(world, 1, false),
                    viz,
                    false);
            // use the pilot assembly with manual control, to test the buttons.
            // player = new PilotAssembly(x -> new ManualPilot(), playerBody, kSpeaker);
        } else {
            player = new RobotAssembly(
                    swerveKinodynamics,
                    x -> SelectorPilot.autonSelector(
                            new Auton(x.getDrive()::getPose,
                                    () -> x.getCamera().recentNoteSightings().values().stream()
                                            .map(NoteSighting::position)
                                            .toList(),
                                    x.getIndexer()::full,
                                    new Pose2d(3.0, 7.5, new Rotation2d(-2.75)), false,
                                    8, 7, 6),
                            new AmpCycler(
                                    x.getDrive()::getPose,
                                    x.getCamera()::noteNearby,
                                    x.getIndexer()::full)),
                    new Player(world, 1, false),
                    viz,
                    false);
        }
        // initially in the upper corner
        player.setState(1.2, 7, Math.PI, 0, 0);

        // do nothing
        friend1 = new RobotAssembly(
                swerveKinodynamics,
                x -> SelectorPilot.autonSelector(
                        new ShootPreload(
                                x.getDrive()::getPose),
                        new SpeakerCycler(
                                x.getDrive()::getPose,
                                x.getCamera()::noteNearby,
                                x.getIndexer()::full,
                                new Pose2d(3.0, 5.5, new Rotation2d(Math.PI)))),
                new Friend("blue 1", world, -1, false),
                viz,
                false);
        // initially near subwoofer
        friend1.setState(0.7, 4.3, 2 * Math.PI / 3, 0, 0); // initial position

        // near 3
        friend2 = new RobotAssembly(
                swerveKinodynamics,
                x -> SelectorPilot.autonSelector(
                        new Auton(
                                x.getDrive()::getPose,
                                () -> x.getCamera().recentNoteSightings().values().stream().map(NoteSighting::position)
                                        .toList(),
                                x.getIndexer()::full,
                                new Pose2d(3.0, 5.5, new Rotation2d(Math.PI)), false,
                                3, 2, 1),
                        new Defender()),
                new Friend("blue 2", world, 0, false),
                viz,
                false);
        // initially at subwoofer
        friend2.setState(1.4, 5.5, Math.PI, 0, 0);

        source = new Source(world, new Translation2d(15.5, 1.0));
        source.setDefaultCommand(new SourceDefault(source, world, true, false));
    }

    @Override
    public void reset() {
        player.reset();
        friend1.reset();
        friend2.reset();
    }

    @Override
    public void begin() {
        player.begin();
        friend1.begin();
        friend2.begin();
    }

    @Override
    public void periodic() {
        player.periodic();
        friend1.periodic();
        friend2.periodic();
    }
}
