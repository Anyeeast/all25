package org.team100.robot;

import org.team100.alliance.Alliance;
import org.team100.alliance.Blue;
import org.team100.alliance.Red;
import org.team100.field.Scorekeeper;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamicsFactory;
import org.team100.lib.planner.ForceViz;
import org.team100.sim.SimWorld;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class RobotContainer {
    private final SimWorld m_world;
    /** Friends */
    private final Alliance m_blue;
    /** Foes */
    private final Alliance m_red;
    private final ForceViz m_viz;

    public RobotContainer(LoggerFactory fieldLogger, SimWorld world) {
        m_world = world;
        m_viz = new ForceViz(fieldLogger);
        final SwerveKinodynamics swerveKinodynamics = SwerveKinodynamicsFactory.get();
        m_blue = new Blue(m_world, m_viz, swerveKinodynamics);
        m_red = new Red(m_world, m_viz, swerveKinodynamics);
        SmartDashboard.putData(CommandScheduler.getInstance());
    }

    public Scorekeeper getScorekeeper() {
        return m_world.getScorekeeper();
    }

    public void robotInit() {
        m_blue.reset();
        m_red.reset();
        m_world.render();
        m_viz.render();
    }

    public void teleopInit() {
        m_blue.begin();
        m_red.begin();
        // m_world.render();
    }

    public void teleopExit() {
        m_blue.reset();
        m_red.reset();
    }

    public void teleopPeriodic() {
        // world.update();
        // world.render();
        m_blue.periodic();
        m_red.periodic();
    }

    /** Sim is updated even when robots are disabled. */
    public void robotPeriodic() {
        m_world.update();
        m_world.render();
        m_viz.render();
    }

    public void autonomousInit() {
        m_blue.begin();
        m_red.begin();
    }

    public void autonomousExit() {
        m_blue.reset();
        m_red.reset();
    }

    public void autonomousPeriodic() {
        m_blue.periodic();
        m_red.periodic();
    }
}
