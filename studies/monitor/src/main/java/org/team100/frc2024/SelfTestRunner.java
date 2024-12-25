package org.team100.frc2024;

import java.util.Set;
import java.util.function.BooleanSupplier;

import org.team100.frc2024.selftest.AmpSelfTest;
import org.team100.lib.commands.drivetrain.Oscillate;
import org.team100.lib.commands.drivetrain.Veering;
import org.team100.lib.commands.drivetrain.manual.DriveManually;
import org.team100.lib.commands.drivetrain.manual.SimpleManualModuleStates;
import org.team100.lib.dashboard.Glassy;
import org.team100.lib.motion.drivetrain.SwerveDriveSubsystem;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamicsFactory;
import org.team100.lib.selftest.DefenseSelfTest;
import org.team100.lib.selftest.DriveManuallySelfTest;
import org.team100.lib.selftest.OscillateSelfTest;
import org.team100.lib.selftest.SelfTestCase;
import org.team100.lib.selftest.SelfTestListener;
import org.team100.lib.selftest.VeeringSelfTest;
import org.team100.lib.logging.SupplierLogger2;
import org.team100.lib.telemetry.Telemetry;
import org.team100.lib.util.ExcludeFromJacocoGeneratedReport;
import org.team100.lib.util.Util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.Subsystem;

/**
 * Run all the test cases sequentially.
 * 
 * This is in the frc2024 package in order to get package-private access to
 * RobotContainer internals.
 */
@ExcludeFromJacocoGeneratedReport
public class SelfTestRunner extends Command implements Glassy {
    public static class SelfTestEnableException extends RuntimeException {
    }

    // You can select which groups of tests to run. Running them
    // all takes a lot of space and long time.
    private static final boolean kTestDrivetrain = false;
    private static final boolean kTestOscillate = false;
    private static final boolean kTestVeering = false;
    private static final boolean kTestMechanisms = false;
    private static final boolean kTestVision = true;

    private static final int kLimit = 10;
    private final RobotContainer m_container;
    private final SequentialCommandGroup m_group;
    private final SelfTestListener m_listener;
    private final BooleanSupplier m_enable;

    /** @param enable for safety this requires holding down a button. */
    public SelfTestRunner(RobotContainer container, BooleanSupplier enable) {
        m_container = container;
        m_enable = enable;
        m_group = new SequentialCommandGroup();
        m_listener = new SelfTestListener();

        // a blocking morse code message to start the test
        // joel 2/22/24 removing for SVR, put it back after that
        // addCase(new InstantCommand(() -> m_container.m_beep.setMessage("TEST")));
        // addCase(m_container.m_beep);
        // this test needs no "treatment" command
        // addCase(new BatterySelfTest(m_container.m_monitor, m_listener));

        SwerveDriveSubsystem drivetrain = m_container.m_drive;
        SupplierLogger2 logger = Telemetry.get().namedRootLogger("SELF TEST");

        if (kTestDrivetrain) {
            // "treatment" is in situ.
            // commented out to simplify the container for comp
            // addCase(new SquareSelfTest(drivetrain, m_listener),
            // m_container.m_driveInALittleSquare);

            // treatment is a specific manual input, supplied by the test case.
            DriveManuallySelfTest driveManuallyTest = new DriveManuallySelfTest(drivetrain, m_listener);

            DriveManually driveManually = new DriveManually(logger, driveManuallyTest::treatment, drivetrain);

            driveManually.register("MODULE_STATE", false,
                    new SimpleManualModuleStates(logger, SwerveKinodynamicsFactory.forTest(logger)));
            driveManually.overrideMode(() -> "MODULE_STATE");
            addCase(driveManuallyTest, driveManually);

            // this only tests the end-state
            addCase(new DefenseSelfTest(drivetrain, m_listener), drivetrain.run(drivetrain::defense));
        }

        if (kTestOscillate) {
            // these take a long time
            addCase(new OscillateSelfTest(drivetrain, m_listener, false, false, 12),
                    new Oscillate(logger, drivetrain));
            addCase(new OscillateSelfTest(drivetrain, m_listener, false, true, 12),
                    new Oscillate(logger, drivetrain));
            addCase(new OscillateSelfTest(drivetrain, m_listener, true, false, 12),
                    new Oscillate(logger, drivetrain));
            addCase(new OscillateSelfTest(drivetrain, m_listener, true, true, 12),
                    new Oscillate(logger, drivetrain));
        }

        if (kTestVeering) {
            // ALERT! This test goes FAAAAAST! ALERT!
            addCase(new VeeringSelfTest(m_listener), new Veering(logger, drivetrain));
        }

        if (kTestMechanisms) {
            // mechanism tests

            // IndexerSelfTest indexerSelfTest = new IndexerSelfTest(container.m_indexer,
            // m_listener);
            // addCase(indexerSelfTest,
            // container.m_indexer.run(indexerSelfTest::treatment));

            AmpSelfTest ampSelfTest = new AmpSelfTest(container.m_ampPivot, m_listener);
            addCase(ampSelfTest, container.m_ampFeeder.run(ampSelfTest::treatment));

            // ShooterSelfTest shooterSelfTest = new ShooterSelfTest(container.m_shooter,
            // m_listener);
            // addCase(shooterSelfTest,
            // container.m_shooter.run(shooterSelfTest::treatment));
        }

        if (kTestVision) {
            // Oscillate is a good choice for vision since it uses acceleration-limited
            // profiles
            // and relatively slow speed. This moves back and forth in x, using
            // module direct mode.
            Oscillate oscillate = new Oscillate(logger, drivetrain);
            // end up where you started
            double expectedDuration = oscillate.getPeriod() * 4;
            addCase(new OscillateSelfTest(drivetrain, m_listener, true, false, expectedDuration), oscillate);
        }

        // since we print to the console we don't want warning noise
        DriverStation.silenceJoystickConnectionWarning(true);
    }

    /**
     * add commands to the group that announce the test being run, and run a new
     * testcase with deadline as the observer of commands.
     */
    private void addCase(Command deadline, Command... commands) {
        m_group.addCommands(new InstantCommand(() -> Util.println("\nRunning " + deadline.getName() + "...")));
        m_group.addCommands(new SelfTestCase(deadline, commands));
    }

    @Override
    public final void initialize() {
        int waitCounter = 0;
        while (!m_enable.getAsBoolean()) {
            if (waitCounter > kLimit) {
                Util.warn("Cancelling self test due to enable");
                cancel();
                return;
            }
            Util.println("Hold down enable (operator start, '8' in sim) to proceed...");
            sleep1();
            waitCounter += 1;
            DriverStation.refreshData();
        }
        m_group.initialize();
    }

    @Override
    public final void execute() {
        m_group.execute();
    }

    @Override
    public final boolean isFinished() {
        if (!m_enable.getAsBoolean()) {
            Util.warn("Aborting test due to enable");
            return true;
        }
        if (m_group.isFinished()) {
            Util.warn("Test complete.");
            return true;
        }
        return false;
    }

    @Override
    public final void end(boolean interrupted) {
        m_group.end(interrupted);
        Util.println(m_listener.summary());
    }

    @Override
    public Set<Subsystem> getRequirements() {
        return m_group.getRequirements();
    }

    private void sleep1() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //
        }
    }

    @Override
    public String getGlassName() {
        return "SelfTestRunner";
    }
}
