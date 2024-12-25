package org.team100.lib.motion.crank;

import java.util.function.Supplier;

/** Impose actuation limits, e.g. for "soft stops" or velocity limits etc. */
public class ActuationFilter implements Actuations {
    private static final double positionMin = 0;
    private static final double positionMax = 1;

    private final Supplier<Actuations> m_supplier;
    private final Supplier<Configurations> m_measurement;

    /**
     * Supply the given actuation when the configuration is within limits, otherwise
     * zero actuation.
     */
    public ActuationFilter(
            Supplier<Actuations> supplier,
            Supplier<Configurations> measurement) {
        m_supplier = supplier;
        m_measurement = measurement;
    }

    @Override
    public Actuation get() {
        double position = m_measurement.get().get().getCrankAngleRad();
        if (position < positionMin || position > positionMax)
            return new Actuation(0.0);
        return m_supplier.get().get();
    }

    @Override
    public void accept(Indicator indicator) {
        m_supplier.get().accept(indicator);
        m_measurement.get().accept(indicator);
        indicator.indicate(this);
    }
}
