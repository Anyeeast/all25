package org.team100.frc2025.Climber;

import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;

public class ClimberVisualization {

    private final Climber m_climber;
    private final ClimberIntake m_intake;
    private final Mechanism2d m_view;
    private final MechanismRoot2d m_root;
    private final MechanismLigament2d m_strut;
    private final MechanismLigament2d m_arm;
    private final MechanismLigament2d m_wheels;

    public ClimberVisualization(Climber climber, ClimberIntake intake) {
        m_climber = climber;
        m_intake = intake;
        m_view = new Mechanism2d(100, 100);
        m_root = m_view.getRoot("root", 50, 0);
        m_strut = new MechanismLigament2d("strut", 30, 90);
        m_root.append(m_strut);
        m_arm = new MechanismLigament2d("arm", 10, 0);
        m_strut.append(m_arm);
        m_wheels = new MechanismLigament2d("wheels", 10, 0);
        m_arm.append(m_wheels);
        SmartDashboard.putData("Climber", m_view);
    }

    public void periodic() {
        m_arm.setAngle(Math.toDegrees(m_climber.angle()));
        m_wheels.setColor(m_intake.isSlow()
                ? new Color8Bit(Color.kRed)
                : new Color8Bit(Color.kGreen));
    }
}
