package org.team100.lib.telemetry;

import org.team100.lib.visualization.AnnunciatorVisualization;

import edu.wpi.first.util.function.BooleanConsumer;
import edu.wpi.first.wpilibj.DigitalOutput;

/**
 * Turns on a digital output in response to boolean alerts, e.g. from the
 * Monitor class.
 */
public class Annunciator implements BooleanConsumer {
    private final DigitalOutput m_pwm;
    private final AnnunciatorVisualization m_viz;
    private boolean state;

    public Annunciator(int channel) {
        m_pwm = new DigitalOutput(channel);
        m_viz = new AnnunciatorVisualization(this);
    }

    @Override
    public void accept(boolean value) {
        m_pwm.set(value);
    }

    public boolean get() {
        return state;
    }

    public void periodic() {
        m_viz.viz();
    }
}
