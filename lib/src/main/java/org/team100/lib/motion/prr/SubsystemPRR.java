package org.team100.lib.motion.prr;

import edu.wpi.first.wpilibj2.command.Subsystem;

public interface SubsystemPRR extends Subsystem {
    /** Position, velocity, and acceleration. May compute dynamic forces too. */
    void set(Config c, JointVelocities jv, JointAccelerations ja);

    /** Current joint positions. */
    Config getConfig();

    /** Current joint velocities. */
    JointVelocities getJointVelocity();
}
