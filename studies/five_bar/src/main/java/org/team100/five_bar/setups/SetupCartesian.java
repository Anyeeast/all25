package org.team100.five_bar.setups;

import org.team100.five_bar.subsystems.FiveBarCartesian;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.Logging;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class SetupCartesian implements Runnable {
    private final FiveBarCartesian m_fiveBar;

    public SetupCartesian() {
        final Logging logging = Logging.instance();
        final LoggerFactory logger = logging.rootLogger;
        XboxController controller = new XboxController(0);

        m_fiveBar = new FiveBarCartesian(logger);
        m_fiveBar.setDefaultCommand(m_fiveBar.position(
                () -> new Translation2d(
                        -1.0 * controller.getRightX(),
                        -1.0 * controller.getRightY())));

        // These bindings are remembered by the trigger event loop, so we don't need to
        // retain them.
        new Trigger(controller::getAButton).whileTrue(m_fiveBar.home());
        new Trigger(controller::getBButton).onTrue(m_fiveBar.zero());

        new Trigger(controller::getXButton).whileTrue(
                Commands.sequence(
                        m_fiveBar.move(new Translation2d(0.0, 0.0)),
                        Commands.waitSeconds(1),
                        m_fiveBar.move(new Translation2d(0.1, 0.0)),
                        Commands.waitSeconds(1),
                        m_fiveBar.move(new Translation2d(0.1, 0.1)),
                        Commands.waitSeconds(1),
                        m_fiveBar.move(new Translation2d(0.0, 0.1)),
                        Commands.waitSeconds(1),
                        m_fiveBar.move(new Translation2d(0.0, 0.0))));
    }

    @Override
    public void run() {
        //
    }

}
