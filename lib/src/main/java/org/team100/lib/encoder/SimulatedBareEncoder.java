package org.team100.lib.encoder;

import java.util.OptionalDouble;

import org.team100.lib.logging.Level;
import org.team100.lib.logging.LoggerFactory;
import org.team100.lib.logging.LoggerFactory.OptionalDoubleLogger;
import org.team100.lib.motor.BareMotor;

import edu.wpi.first.wpilibj.Timer;

public class SimulatedBareEncoder implements IncrementalBareEncoder {
    private final BareMotor m_motor;

    // accumulates.
    private double m_position = 0;
    private double m_time = Timer.getFPGATimestamp();
    private OptionalDoubleLogger m_log_position;
    private OptionalDoubleLogger m_log_velocity;

    public SimulatedBareEncoder(
            LoggerFactory parent,
            BareMotor motor) {
        LoggerFactory child = parent.child(this);
        m_motor = motor;
        m_log_position = child.optionalDoubleLogger(Level.TRACE, "position (rad)");
        m_log_velocity = child.optionalDoubleLogger(Level.TRACE, "velocity (rad_s)");
    }

    /** Cached. */
    @Override
    public OptionalDouble getVelocityRad_S() {
        double m_rate = m_motor.getVelocityRad_S();
        return OptionalDouble.of(m_rate);
    }

    /**
     * Cached, almost.
     * Derives position by integrating velocity over one time step.
     */
    @Override
    public OptionalDouble getPositionRad() {
        double now = Timer.getFPGATimestamp();
        double dt = now - m_time;
        double m_rate = m_motor.getVelocityRad_S();
        m_position += m_rate * dt;
        m_time = now;
        return OptionalDouble.of(m_position);
    }

    @Override
    public void reset() {
        m_position = 0;
        m_time = Timer.getFPGATimestamp();
    }

    @Override
    public void close() {
        //
    }

    @Override
    public void setEncoderPositionRad(double motorPositionRad) {
        m_motor.setEncoderPositionRad(motorPositionRad);
    }

    @Override
    public void periodic() {
        m_log_position.log(this::getPositionRad);
        m_log_velocity.log(this::getVelocityRad_S);
    }

    @Override
    public double getPositionBlockingRad() {
        return getPositionRad().getAsDouble();
    }
}
