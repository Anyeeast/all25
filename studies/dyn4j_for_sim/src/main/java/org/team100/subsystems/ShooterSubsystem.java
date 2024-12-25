package org.team100.subsystems;

import org.team100.robot.RobotAssembly;
import org.team100.sim.RobotBody;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.interpolation.InterpolatingDoubleTreeMap;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/** The shooter is used for shooting, lobbing, and amping. */
public class ShooterSubsystem extends SubsystemBase {

    /** Height of the shooter exit, measured from the floor. */
    private static final double kShooterHeightM = 0.5;

    /** Real exit velocity 20 m/s * note mass 0.235 kg. */
    private static final double kShootImpulseNs = 4.7;

    /** Enough to get about 3.5m high and travel about 6m. */
    private static final double kLobImpulseNs = 2.6;

    /** This is a guess. */
    private static final double kLobElevationRad = -0.9;

    /** Just enough to reach the amp height. */
    private static final double kAmpImpulseNs = 1.2;

    /** Slightly beyond vertical. */
    // private static final double kAmpElevationRad = -1.66;
    private static final double kAmpElevationRad = -1.6;

    private final RobotAssembly m_assembly;
    private final RobotBody m_robotBody;
    private final Translation2d m_speakerPosition;
    private final boolean m_debug;
    /** range (meters): pitch (radians) */
    private final InterpolatingDoubleTreeMap shooterMap;

    public ShooterSubsystem(
            RobotAssembly assembly,
            RobotBody robotBody,
            boolean debug) {
        m_assembly = assembly;
        m_robotBody = robotBody;
        m_speakerPosition = robotBody.speakerPosition();
        m_debug = debug;
        shooterMap = new InterpolatingDoubleTreeMap();
        populateShooterMap();
    }

    public void shoot() {
        if (m_debug)
            System.out.println("shoot");
        double rangeM = m_robotBody.getPose().getTranslation().getDistance(m_speakerPosition);
        double elevationRad = shooterMap.get(rangeM);
        printShot(rangeM, elevationRad);
        shooter(kShootImpulseNs, elevationRad);
    }

    public void lob() {
        if (m_debug)
            System.out.println("lob");
        shooter(kLobImpulseNs, kLobElevationRad);
    }

    public void amp() {
        if (m_debug)
            System.out.println("amp");
        shooter(kAmpImpulseNs, kAmpElevationRad);
    }

    ////////////////////////////////////////////////////////

    /** This is for tuning the shooter map. */
    private void printShot(double range, double elevationRad) {
        if (m_assembly.m_indexerShooterHandoff == null)
            return;
        if (m_debug)
            System.out.printf("shoot %s range %5.3f elevation %5.3f\n",
                    m_assembly.m_indexerShooterHandoff, range, elevationRad);
    }

    /**
     * @param impulseNs    measured in newton-seconds.
     * @param elevationRad pitch angle (positive down) from the "front", for amp,
     *                     use >pi/2.
     */
    private void shooter(double impulseNs, double elevationRad) {
        if (m_assembly.m_indexerShooterHandoff == null) {
            if (m_debug)
                System.out.println("Shooter received no note!");
            return;
        }
        if (m_debug)
            System.out.println("shooting note " + m_assembly.m_indexerShooterHandoff.getUserData());
        double yawRad = m_robotBody.getPose().getRotation().getRadians();
        Rotation3d rot3d = new Rotation3d(0, elevationRad, yawRad);
        Translation3d impulse3d = new Translation3d(impulseNs, rot3d);
        m_assembly.m_indexerShooterHandoff.applyImpulse(impulse3d);
        m_assembly.m_indexerShooterHandoff.setAltitude(kShooterHeightM);
        m_assembly.m_indexerShooterHandoff = null;
    }

    /**
     * I experimented in the sim and then smoothed the curve by eye.
     * https://docs.google.com/spreadsheets/d/13UDvGggbjgwxa30aaze3LuQ9peW3PO5ZXYqGSjVjXBQ/edit#
     */
    private void populateShooterMap() {
        shooterMap.put(0.0, -1.571); // pi/2
        shooterMap.put(0.5, -1.390);
        shooterMap.put(1.0, -1.220); // 1.29 is min feasible range
        shooterMap.put(1.5, -1.050);
        shooterMap.put(2.0, -0.880);
        shooterMap.put(2.5, -0.720);
        shooterMap.put(3.0, -0.600);
        shooterMap.put(3.5, -0.520);
        shooterMap.put(4.0, -0.460);
        shooterMap.put(4.5, -0.420);
        shooterMap.put(5.0, -0.400);
        shooterMap.put(5.5, -0.380);
        shooterMap.put(6.0, -0.360);
        shooterMap.put(6.5, -0.350);
        shooterMap.put(7.0, -0.340);
        shooterMap.put(7.5, -0.330); // beyond max feasible range
    }

}
