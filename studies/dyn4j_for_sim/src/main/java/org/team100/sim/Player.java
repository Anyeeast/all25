package org.team100.sim;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * Controls apply force and torque.
 */
public class Player extends RobotBody {

    private final double m_yBias;

    /** Note: adds this to the world. */
    public Player(SimWorld world, double yBias, boolean debug) {
        super("player", world, debug);
        m_yBias = yBias;
    }

    @Override
    public boolean friend(RobotBody other) {
        // only one player so only friends are friends
        return other instanceof Friend;
    }

    @Override
    public Pose2d ampPosition() {
        return Friend.kAmpSpot;
    }

    @Override
    public Pose2d sourcePosition() {
        return Friend.kSource;
    }

    @Override
    public Pose2d opponentSourcePosition() {
        return Foe.kSource;
    }

    @Override
    public Pose2d defenderPosition() {
        return Friend.kDefendSpot;
    }

    @Override
    public Pose2d passingPosition() {
        return Friend.kPassingSpot;
    }

    @Override
    public Translation2d speakerPosition() {
        return Friend.kSpeaker;
    }

    @Override
    public double yBias() {
        return m_yBias;
    }
}
