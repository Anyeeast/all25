package org.team100.lib.commands.semiauto;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.function.Supplier;

import org.team100.lib.camera.RobotSighting;
import org.team100.lib.motion.drivetrain.DriveSubsystemInterface;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeDelta;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeVelocity;
import org.team100.lib.motion.drivetrain.kinodynamics.SwerveKinodynamics;
import org.team100.lib.planner.ForceViz;
import org.team100.lib.planner.Tactics;
import org.team100.lib.util.Debug;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Stay between opponents and their source.
 * 
 * Never finishes.
 * 
 * note low defense skill level to try to balance the game
 */
public class DefendSource extends Command {
    // this is quite low, to try to make the game more balanced: up to 8 works well
    private static final int kDistanceFromSource = 4;

    /** push towards the opponent */
    private static final double kDefensePushing = 5;
    /** absolutely do not get pushed into the protected area */
    private static final double kCornerRepulsion = -50;
    /** get back to the spot fast if nothing else is happening */
    private static final double kWaitingAttraction = 5;
    /** try very hard stay between the opponent and the corner */
    private static final double kCorner = 50;

    private final SwerveKinodynamics m_swerveKinodynamics;

    /**
     * it is easy for good defense to shut down the game completely; this discounts
     * the defense quickness.
     */
    private final double m_skill;
    private final DriveSubsystemInterface m_drive;
    private final Supplier<NavigableMap<Double, RobotSighting>> m_camera;
    private final Supplier<Pose2d> m_position;
    private final Supplier<Pose2d> m_source;
    private final boolean m_debug;
    private final Tactics m_tactics;
    private final ForceViz m_viz;

    private int m_pinCounter = 0;

    public DefendSource(
            SwerveKinodynamics swerveKinodynamics,
            double skill,
            DriveSubsystemInterface drive,
            Supplier<NavigableMap<Double, RobotSighting>> camera,
            Supplier<Pose2d> position,
            Supplier<Pose2d> source,
            Tactics tactics,
            ForceViz viz,
            boolean debug) {
        m_swerveKinodynamics = swerveKinodynamics;
        m_skill = skill;
        m_drive = drive;
        m_camera = camera;
        m_position = position;
        m_source = source;
        m_debug = debug && Debug.enable();
        m_tactics = tactics;
        m_viz = viz;
        addRequirements(drive);
    }

    @Override
    public void execute() {
        if (m_debug)
            System.out.print("Defend");
        Pose2d pose = m_drive.getPose();
        if (m_debug)
            System.out.printf(" pose (%5.2f,%5.2f)", pose.getX(), pose.getY());
        FieldRelativeVelocity desired = work(
                m_skill,
                pose,
                m_position.get(),
                m_source.get(),
                m_camera.get());
        if (m_debug)
            m_viz.desired(pose.getTranslation(), desired);
        if (m_debug)
            System.out.printf(" desired %s", desired);
        FieldRelativeVelocity v = m_tactics.apply(desired);
        if (m_debug)
            System.out.printf(" tactics %s", v);
        v = v.plus(desired);
        v = v.clamp(m_swerveKinodynamics.getMaxDriveVelocityM_S(), m_swerveKinodynamics.getMaxAngleSpeedRad_S());
        if (m_debug)
            System.out.printf(" final %s\n", v);
        m_drive.drive(v);
    }

    /**
     * If no robots are around, wait near the source. If there is a foe, stay
     * between it and the source. Avoid getting too close to the source.
     */
    private FieldRelativeVelocity work(
            double m_skill,
            Pose2d pose,
            Pose2d defenderPosition,
            Pose2d opponentSourcePosition,
            NavigableMap<Double, RobotSighting> recentSightings) {
        FieldRelativeVelocity v = new FieldRelativeVelocity(0, 0, 0);

        // attract to the waiting spot but only if nothing else is happening
        FieldRelativeDelta toWaitingSpot = FieldRelativeDelta.delta(pose, defenderPosition)
                .limit(1, 1);
        FieldRelativeVelocity waiting = new FieldRelativeVelocity(
                toWaitingSpot.getX(),
                toWaitingSpot.getY(),
                0).times(kWaitingAttraction);
        if (m_debug)
            System.out.printf(" waiting %s", waiting);
        v = v.plus(waiting);

        // repel from the corner (1/r), and don't chase opponents, if too close
        // (to avoid fouling)
        FieldRelativeDelta toCorner = FieldRelativeDelta.delta(pose, opponentSourcePosition);
        if (toCorner.getTranslation().getNorm() < 2.0) {
            double magnitude = m_skill * kCornerRepulsion
                    / (toCorner.getTranslation().getNorm() * toCorner.getTranslation().getNorm());
            FieldRelativeVelocity repel = new FieldRelativeVelocity(
                    toCorner.getX(),
                    toCorner.getY(),
                    0).times(magnitude);
            if (m_debug)
                System.out.printf(" repel %s", repel);
            // v = v.plus(repel);
            v = repel;
            return v;
        }
        // give up if too far
        if (toCorner.getTranslation().getNorm() > 6) {
            System.out.print(" too far");
            return v;
        }
        for (Entry<Double, RobotSighting> mostRecent : recentSightings.entrySet()) {
            RobotSighting mostRecentSighting = mostRecent.getValue();
            // don't try to defend friends
            // TODO: stay out of their way
            if (mostRecentSighting.friend())
                continue;
            Translation2d foe = mostRecentSighting.position();

            double distanceFromMe = pose.getTranslation().getDistance(foe);
            if (distanceFromMe > 6) {
                // don't react to far-away obstacles
                continue;
            }

            // avoid pinning penalties
            if (distanceFromMe < 2) {
                m_pinCounter++;
                if (m_pinCounter > 150)
                    m_pinCounter = 0;
                if (m_pinCounter > 100)
                    return v;

            }

            double distanceFromSource = opponentSourcePosition.getTranslation().getDistance(foe);
            if (distanceFromSource > kDistanceFromSource) {
                // don't chase it too far
                continue;
            }
            if (m_debug)
                System.out.printf(" foe (%5.2f, %5.2f)", foe.getX(), foe.getY());
            // drive towards the opponent
            Translation2d toOpponent = foe.minus(pose.getTranslation());
            Translation2d force = toOpponent.times(
                    m_skill * kDefensePushing / Math.pow(toOpponent.getNorm(), 2));
            FieldRelativeVelocity push = new FieldRelativeVelocity(force.getX(), force.getY(), 0);
            if (m_debug)
                System.out.printf(" push %s", push);
            // v = v.plus(push);
            v = push;
            // try to get to the line between the opponent and the source
            Translation2d sourceToFoe = foe.minus(opponentSourcePosition.getTranslation());
            sourceToFoe = sourceToFoe.div(sourceToFoe.getNorm());
            Translation2d sourceToMe = pose.getTranslation().minus(opponentSourcePosition.getTranslation());
            sourceToMe = sourceToMe.div(sourceToMe.getNorm());
            Translation2d toFoe = sourceToFoe.minus(sourceToMe);
            FieldRelativeVelocity corner = new FieldRelativeVelocity(
                    toFoe.getX() * m_skill * kCorner,
                    toFoe.getY() * m_skill * kCorner, 0);
            if (m_debug)
                System.out.printf(" corner %s", corner);
            v = v.plus(corner);

            break;
        }
        return v;
    }

}
