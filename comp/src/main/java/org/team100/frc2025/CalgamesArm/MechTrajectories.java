package org.team100.frc2025.CalgamesArm;

import java.util.List;

import org.team100.lib.commands.Done;
import org.team100.lib.geometry.HolonomicPose2d;
import org.team100.lib.motion.kinematics.AnalyticalJacobian;
import org.team100.lib.motion.kinematics.ElevatorArmWristKinematics;
import org.team100.lib.motion.kinematics.JointAccelerations;
import org.team100.lib.motion.kinematics.JointVelocities;
import org.team100.lib.trajectory.TrajectoryPlanner;
import org.team100.lib.trajectory.timing.ConstantConstraint;
import org.team100.lib.trajectory.timing.JointConstraint;
import org.team100.lib.trajectory.timing.TimingConstraint;
import org.team100.lib.trajectory.timing.YawRateConstraint;

import edu.wpi.first.wpilibj2.command.Command;

/** Make a trajectory from the start to the end and follow it. */
public class MechTrajectories extends Command {
    private final CalgamesMech m_subsystem;
    private final TrajectoryPlanner m_planner;

    public MechTrajectories(
            CalgamesMech mech,
            ElevatorArmWristKinematics k,
            AnalyticalJacobian j) {
        m_subsystem = mech;
        List<TimingConstraint> c = List.of(
                // NOTE! the JointConstraint is new; you might want to play with it, or remove
                // it and use the ConstantConstraint instead.
                 new ConstantConstraint(10, 5),
                 new YawRateConstraint(10, 5)
                // new JointConstraint(
                //         k,
                //         j,
                //         new JointVelocities(8, 10, 10),
                //         new JointAccelerations(16, 16, 16))
                        );
        m_planner = new TrajectoryPlanner(c);
    }

    /** A command that goes from the start to the end and then finishes. */
    public Command terminal(String name, HolonomicPose2d start, HolonomicPose2d end) {

        // FollowTrajectory f = new FollowTrajectory(
        // m_subsystem, m_planner.restToRest(List.of(start, end)));

        /** Use the start course and ignore the start pose for now */
        Done f = new GoToPoseCalGamesMech(m_subsystem, start.course(), end, m_planner);
        return f
                .until(f::isDone)
                .withName(name);
    }

    /** A command that goes from the start to the end and then waits forever. */
    public Done endless(String name, HolonomicPose2d start, HolonomicPose2d end) {

        /** Use the start course and ignore the start pose for now */
        GoToPoseCalGamesMech c = new GoToPoseCalGamesMech(m_subsystem, start.course(), end, m_planner);
        c.setName(name);
        return c;
        // return new FollowTrajectory(m_subsystem, m_planner.restToRest(List.of(start,
        // end)));
    }

}
