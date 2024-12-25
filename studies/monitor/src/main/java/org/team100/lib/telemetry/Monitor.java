package org.team100.lib.telemetry;

import java.util.function.BooleanSupplier;

import org.team100.lib.config.Identity;
import org.team100.lib.dashboard.Glassy;
import org.team100.lib.logging.SupplierLogger2;
import org.team100.lib.logging.SupplierLogger2.BooleanSupplierLogger2;
import org.team100.lib.logging.SupplierLogger2.DoubleSupplierLogger2;
import org.team100.lib.telemetry.Telemetry.Level;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.RobotController;

/**
 * Measures some things about the robot.
 * Writes them to the log.
 * Exposes them to self testing.
 * Sets the annunciator if bounds are exceeded.
 */
public class Monitor implements Glassy {
    private final Annunciator m_annunciator;
    private final BooleanSupplier m_test;
    private final PowerDistribution m_pdp;
    private final DoubleSupplierLogger2 m_log_voltage;
    private final BooleanSupplierLogger2 m_log_master;

    private boolean m_shouldAlert;

    /**
     * @param annunciator some sort of alert.
     * @param test        activates the annunciator, to make sure it's working.
     */
    public Monitor(SupplierLogger2 parent, Annunciator annunciator, BooleanSupplier test) {
        SupplierLogger2 child = parent.child(this);
        m_log_voltage = child.doubleLogger(Level.COMP, "battery_voltage");
        m_log_master = child.booleanLogger(Level.COMP, "master_warning");
        m_annunciator = annunciator;
        m_test = test;
        m_pdp = new PowerDistribution(1, ModuleType.kRev);
    }

    public void periodic() {
        m_shouldAlert = false;
        // this should test different things for different identities.
        if (Identity.instance == Identity.COMP_BOT || Identity.instance == Identity.BETA_BOT) {
            m_log_voltage.log(this::getBatteryVoltage);
            // TODO: fix the pdp observer
            // t.log(Level.INFO, m_name, "bus_voltage", getBusVoltage());
            // t.log(Level.INFO, m_name, "total_current", getTotalCurrent());
            // for (int i = 0; i < 16; ++i) {
            // t.log(Level.INFO, m_name, String.format("channel_current_%02d", i),
            // getChannelCurrent(i));
            // }
        }

        if (m_test.getAsBoolean())
            m_shouldAlert = true;
        m_log_master.log(() -> m_shouldAlert);
        m_annunciator.accept(m_shouldAlert);
        m_annunciator.periodic();
    }

    public double getBatteryVoltage() {
        double voltage = RobotController.getBatteryVoltage();
        if (voltage < 11 || voltage > 14) {
            m_shouldAlert = true;
        }
        return voltage;
    }

    public double getBusVoltage() {
        double voltage = m_pdp.getVoltage();
        if (voltage < 11 || voltage > 14) {
            m_shouldAlert = true;
        }
        return voltage;
    }

    public double getTotalCurrent() {
        double current = m_pdp.getTotalCurrent();
        if (current > 120) {
            m_shouldAlert = true;
        }
        return current;
    }

    public double getChannelCurrent(int channel) {
        double current = m_pdp.getCurrent(channel);
        if (current > 40) {
            m_shouldAlert = true;
        }
        return current;
    }

    @Override
    public String getGlassName() {
        return "Monitor";
    }

}
