package org.team100.lib.planner;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.team100.lib.camera.RobotSighting;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeVelocity;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.util.Debug;

import edu.wpi.first.math.geometry.Pose2d;

/**
 * Low level drive motion heuristics that can be used by any command.
 * 
 * Pointwise repulsive forces are inversely proportional to distance, like
 * gravity or electrostatics in two dimensions.
 */
public class Tactics implements UnaryOperator<FieldRelativeVelocity> {
    private final SwerveKinodynamics m_swerveKinodynamics;
    private final Supplier<Pose2d> m_drive;
    private final Supplier<NavigableMap<Double, RobotSighting>> m_camera;
    private final List<Tactic> m_tactics;
    private final ForceViz m_viz;
    private final boolean m_debug;

    /**
     * 
     * @param drive
     * @param camera
     * @param avoidObstacles defenders don't care about obstacles
     * @param avoidEdges     some goals are near the edge, so turn this off,
     *                       otherwise turning bumps into the edge.
     * @param avoidRobots    defenders don't care about robots, also picking can be
     *                       aggressive
     * @param debug
     */
    public Tactics(
            SwerveKinodynamics swerveKinodynamics,
            Supplier<Pose2d> drive,
            Supplier<NavigableMap<Double, RobotSighting>>  camera,
            ForceViz viz,
            boolean avoidObstacles,
            boolean avoidEdges,
            boolean avoidRobots,
            boolean debug) {
        m_swerveKinodynamics = swerveKinodynamics;
        m_drive = drive;
        m_camera = camera;
        m_viz = viz;
        m_tactics = new ArrayList<>();
        if (avoidObstacles) {
            m_tactics.add(new SteerAroundObstacles(m_drive, viz, debug));
            m_tactics.add(new ObstacleRepulsion(m_drive, viz, debug));
        }
        if (avoidEdges) {
            m_tactics.add(new AvoidEdges(m_drive, viz, debug));
            m_tactics.add(new AvoidSubwoofers(m_drive, viz, debug));
        }
        if (avoidRobots) {
            m_tactics.add(new SteerAroundRobots(m_drive, m_camera, viz, debug));
            m_tactics.add(new RobotRepulsion(m_drive, m_camera, viz, debug));
        }
        m_debug = debug && Debug.enable();
    }

    /** add tactics to desired. */
    public FieldRelativeVelocity finish(FieldRelativeVelocity desired) {
        if (m_debug)
            m_viz.desired(m_drive.get().getTranslation(), desired);
        if (m_debug)
            System.out.printf(" desire %s", desired);
        FieldRelativeVelocity v = apply(desired);
        v = v.plus(desired);
        v = v.clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
        if (m_debug)
            System.out.printf(" final %s\n", v);
        return v;
    }

    /** Output is clamped to feasible v and omega. */
    @Override
    public FieldRelativeVelocity apply(FieldRelativeVelocity desired) {
        FieldRelativeVelocity v = new FieldRelativeVelocity(0, 0, 0);
        for (Tactic t : m_tactics) {
            v = v.plus(t.apply(desired));
        }
        v = v.clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
        if (m_debug)
            System.out.printf(" tactic %s", v);
        return v;
    }

}
