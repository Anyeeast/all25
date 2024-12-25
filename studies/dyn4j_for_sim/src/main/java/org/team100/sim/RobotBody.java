package org.team100.sim;


import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.team100.lib.motion.drivetrain.kinodynamics.FieldRelativeVelocity;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public abstract class RobotBody extends Body100 {
    /** All robots are squares about 30 inches on a side. */
    protected static final double kRobotSize = 0.75;
    /** All robots can fit under the stage but also drive over notes. */
    private static final Range kVertical = new Range(0.1, 0.7);

    private final SimWorld m_world;

    /** Note: adds this to the world. */
    protected RobotBody(String id, SimWorld world, boolean debug) {
        super(id, debug);
        m_world = world;

        // about 30 inches including bumpers == 24 inch frame
        // 100 kg/m2 implies about 120 lbs
        // 0.5 friction is a total guess
        // 0.1 restitution: bumpers are not springy
        BodyFixture fixture = addFixture(
                Geometry.createSquare(kRobotSize),
                100,
                0.5,
                0.1);
        // this means the springiness doesn't change with velocity
        fixture.setRestitutionVelocity(0.0);
        fixture.setFilter(this);
        setMass(MassType.NORMAL);
        // fiddled with damping until it seemed "right"
        setAngularDamping(2);
        setLinearDamping(2);
        m_world.addBody(this);
    }

    public String getName() {
        return m_id;
    }

    /**
     * The only joint used for a RobotBody is to a note in the indexer.
     */
    public boolean carryingNote() {        
        return getWorld().getJointCount(this) > 0;
    }

    public abstract boolean friend(RobotBody body);

    public abstract Pose2d ampPosition();

    public abstract Pose2d sourcePosition();

    public abstract Pose2d opponentSourcePosition();

    public abstract Pose2d defenderPosition();

    public abstract Pose2d passingPosition();

    public abstract Translation2d speakerPosition();

    /** For "lanes" this is a bias in output Y velocity. */
    public abstract double yBias();

    public SimWorld getWorld() {
        return m_world;
    }

    public Pose2d getPose() {
        Transform simTransform = transform;
        Vector2 translation = simTransform.getTranslation();
        double angle = simTransform.getRotationAngle();
        return new Pose2d(
                translation.x,
                translation.y,
                new Rotation2d(angle));
    }

    public double getRotationAngle() {
        return transform.getRotationAngle();
    }

    public FieldRelativeVelocity getVelocity() {
        return new FieldRelativeVelocity(
                linearVelocity.x,
                linearVelocity.y,
                angularVelocity);
    }

    @Override
    public Range getVerticalExtent() {
        return kVertical;
    }

}
