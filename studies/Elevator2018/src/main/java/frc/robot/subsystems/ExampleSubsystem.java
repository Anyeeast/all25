// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class ExampleSubsystem extends SubsystemBase {

  private final CANSparkMax m_motor;

  /** Creates a new ExampleSubsystem. */
  public ExampleSubsystem(
      CANSparkMax motor) {
    m_motor = motor;
    m_motor.enableVoltageCompensation(12.0);
    // m_motor.setSmartCurrentLimit(25);
    m_motor.setSecondaryCurrentLimit(0);
    m_motor.setIdleMode(IdleMode.kCoast);
  }

  public void set(double value) {
    m_motor.set(value);
  }

  public void reset() {
    m_motor.getEncoder().setPosition(0);
  }

  public double get() {
    return m_motor.getEncoder().getPosition();
  }

  /**
   * Example command factory method.
   *
   * @return a command
   */
  public CommandBase exampleMethodCommand() {
    // Inline construction of command goes here.
    // Subsystem::RunOnce implicitly requires `this` subsystem.
    return runOnce(
        () -> {
          /* one-time action goes here */
        });
  }

  /**
   * An example method querying a boolean state of the subsystem (for example, a
   * digital sensor).
   *
   * @return value of some boolean subsystem state, such as a digital sensor.
   */
  public boolean exampleCondition() {
    // Query some boolean state, such as a digital sensor.
    return false;
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Measurment", get());
    SmartDashboard.putNumber("Current", m_motor.getOutputCurrent());

  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
