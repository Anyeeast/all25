package org.team100.five_bar.setups;

import org.team100.five_bar.subsystems.FiveBarMech;
import org.team100.five_bar.subsystems.Pen;
import org.team100.five_bar.visualization.FiveBarVisualization;
import org.team100.lib.logging.Level;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.Logging;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;

public class SetupMech implements Runnable {
    private final double CONTROL_SCALE = 0.1;
    private final FiveBarMech m_fiveBar;
    private final Pen m_pen;
    private final FiveBarVisualization m_viz;

    public SetupMech() {
        final Logging logging = Logging.instance();
        logging.setLevel(Level.TRACE);
        final LoggerFactory logger = logging.rootLogger;
        XboxController controller = new XboxController(0);

        m_fiveBar = new FiveBarMech(logger);
        m_pen = new Pen();
        m_viz = new FiveBarVisualization(m_fiveBar::getJointPositions);
        m_fiveBar.setDefaultCommand(m_fiveBar.position(
                () -> CONTROL_SCALE * controller.getLeftX(), // axis 0, "a" and "d" in the sim
                () -> CONTROL_SCALE * controller.getLeftY())); // axis 1, "w" and "s" in the sim

        // These bindings are remembered by the trigger event loop, so we don't need to
        // retain them.
        // button 1, "z" in the sim
        new Trigger(controller::getAButton).whileTrue(m_fiveBar.home());
        // button 2, "x" in the sim
        new Trigger(controller::getBButton).onTrue(m_fiveBar.zero());
        // button 3, "c" in the sim
        new Trigger(controller::getXButton).onTrue(m_pen.down());
        // button 4, "v" in the sim.
        new Trigger(controller::getYButton).onTrue(m_pen.up());
    }

    @Override
    public void run() {
        m_viz.periodic();
    }

}
