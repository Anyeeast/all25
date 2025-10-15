package org.team100.lib.motor;

import java.util.function.Supplier;

import org.team100.lib.config.PIDConstants;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.configs.AudioConfigs;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.Slot1Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfigurator;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

/** Utilities for CTRE Phoenix motors: Falcon, Kraken. */
public class Phoenix100 {
    private static final boolean ACTUALLY_CRASH = false;
    /**
     * The default is 0.05, so this is double, to eliminate unnecessary config
     * failures.
     */
    private static final double TIMEOUT_SEC = 0.3;

    public static void logCrashStatus() {
        if (ACTUALLY_CRASH)
            System.out.println("WARNING: " + "***** Motor config fail will CRASH the robot, NOT FOR COMP! *****");
        // else
        // StrUtil.warn("***** Motor config fail will not be caught, NOT FOR DEV! *****");

    }

    public static void crash(Supplier<StatusCode> s) {
        StatusCode statusCode = s.get();
        if (statusCode.isError()) {
            if (ACTUALLY_CRASH)
                throw new IllegalStateException(statusCode.toString());
            System.out.println("WARNING: " + "******************************************************");
            System.out.println("WARNING: " + "****** MOTOR CONFIG HAS FAILED MOTOR IS NOT SET CORRECTLY ******");
            System.out.println("WARNING: " + statusCode.toString());
        }
    }

    public static void warn(Supplier<StatusCode> s) {
        StatusCode statusCode = s.get();
        if (statusCode.isError()) {
            System.out.println("WARNING: " + statusCode.toString());
        }
    }

    public static void baseConfig(TalonFXConfigurator conf) {
        TalonFXConfiguration base = new TalonFXConfiguration();
        crash(() -> conf.apply(base, TIMEOUT_SEC));
    }

    public static void motorConfig(
            TalonFXConfigurator conf,
            NeutralMode neutral,
            MotorPhase phase) {
        MotorOutputConfigs motorConfigs = new MotorOutputConfigs();
        motorConfigs.NeutralMode = switch (neutral) {
            case COAST -> NeutralModeValue.Coast;
            case BRAKE -> NeutralModeValue.Brake;
        };
        motorConfigs.Inverted = switch (phase) {
            case FORWARD -> InvertedValue.CounterClockwise_Positive;
            case REVERSE -> InvertedValue.Clockwise_Positive;
        };
        crash(() -> conf.apply(motorConfigs, TIMEOUT_SEC));
    }

    /**
     * @see https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html
     * @see https://www.chiefdelphi.com/t/the-brushless-era-needs-sensible-default-current-limits/461056/51
     */
    public static void currentConfig(TalonFXConfigurator conf, double supply, double stator) {
        CurrentLimitsConfigs currentConfigs = new CurrentLimitsConfigs();
        currentConfigs.SupplyCurrentLimit = supply;
        currentConfigs.SupplyCurrentLimitEnable = true;
        currentConfigs.StatorCurrentLimit = stator;
        currentConfigs.StatorCurrentLimitEnable = true;
        crash(() -> conf.apply(currentConfigs, TIMEOUT_SEC));
    }

    /**
     * parameter units depend on the mode. We use velocityvoltage for velocity
     * control, so the units would be volts per rev/s. For position control we use
     * positionvoltage, so the units would be volts per revolution.
     */
    public static void pidConfig(TalonFXConfigurator conf, PIDConstants pid) {
        Slot0Configs slot0Configs = new Slot0Configs();
        Slot1Configs slot1Configs = new Slot1Configs();
        slot0Configs.kV = 0.0; // we use "arbitrary feedforward", not this.
        slot1Configs.kV = 0.0;
        slot0Configs.kP = pid.getPositionP();
        slot0Configs.kI = pid.getPositionI();
        slot0Configs.kD = pid.getPositionD();
        slot1Configs.kP = pid.getVelocityP();
        slot1Configs.kI = pid.getVelocityI();
        slot1Configs.kD = pid.getVelocityD();
        crash(() -> conf.apply(slot0Configs, TIMEOUT_SEC));
        crash(() -> conf.apply(slot1Configs, TIMEOUT_SEC));
    }

    /** Allow music during disable: this is for warning sounds. */
    public static void audioConfig(TalonFXConfigurator conf) {
        AudioConfigs audioConfigs = new AudioConfigs();
        audioConfigs.AllowMusicDurDisable = true;
        crash(() -> conf.apply(audioConfigs, TIMEOUT_SEC));
    }

    private Phoenix100() {
        //
    }

}
