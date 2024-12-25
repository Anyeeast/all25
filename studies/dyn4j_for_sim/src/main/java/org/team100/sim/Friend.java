package org.team100.sim;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * Friends try to pick from the source and score in the amp corner.
 * 
 * Friends should have human-readable names.
 * 
 * TODO: spin away if close to an opponent
 */
public class Friend extends RobotBody {
    /**
     * Intake is on the back, robot usually comes from behind, so source pick
     * rotation target is 180, i.e. arrive in reverse.
     */
    static final Pose2d kSource = new Pose2d(15.5, 2, new Rotation2d(Math.PI));
    /** This is the robot center when facing the amp */
    static final Pose2d kAmpSpot = new Pose2d(1.840, 7.5, new Rotation2d(-Math.PI / 2));
    static final Pose2d kPassingSpot = new Pose2d(9.5, 1, new Rotation2d(3 * Math.PI / 4));
    static final Pose2d kDefendSpot = new Pose2d(3.5, 2, new Rotation2d());
    /** Center of the speaker: the target to shoot at. */
    static final Translation2d kSpeaker = new Translation2d(0, 5.548);

    private final double m_yBias;

    /** Note: adds this to the world. */
    public Friend(String id, SimWorld world, double yBias, boolean debug) {
        super(id, world, debug);
        m_yBias = yBias;
    }

    @Override
    public boolean friend(RobotBody other) {
        // either friends or player
        return other instanceof Friend
                || other instanceof Player;
    }

    @Override
    public Pose2d ampPosition() {
        return kAmpSpot;
    }

    @Override
    public Pose2d sourcePosition() {
        return kSource;
    }

    @Override
    public Pose2d opponentSourcePosition() {
        return Foe.kSource;
    }

    @Override
    public Pose2d defenderPosition() {
        return kDefendSpot;
    }

    @Override
    public Pose2d passingPosition() {
        return kPassingSpot;
    }

    @Override
    public Translation2d speakerPosition() {
        return kSpeaker;
    }

    @Override
    public double yBias() {
        return m_yBias;
    }

}
