package org.team100.lib.motion.urdf;

import java.util.List;

import org.team100.lib.motion.urdf.URDFJoint.JointType;
import org.team100.lib.motion.urdf.URDFJoint.Limit;

import edu.wpi.first.math.Nat;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.numbers.N3;

/**
 * Cartesian prismatic, like the Shopsabre:
 * 
 * * base is the table at the origin
 * * gantry moves in y
 * * headstock moves in x (along the gantry)
 * * spindle moves in z (along the headstock)
 * 
 * This should be very easy to solve :-)
 */
public class URDFCartesian extends URDFRobot<N3> {
    private URDFCartesian(String name, List<URDFLink> links, List<URDFJoint> joints) {
        super(Nat.N3(), name, links, joints);
    }

    public static URDFCartesian make() {
        URDFLink base = new URDFLink("base");
        URDFLink gantry = new URDFLink("gantry");
        URDFLink head_stock = new URDFLink("head_stock");
        URDFLink spindle = new URDFLink("spindle");
        URDFLink tool_center_point = new URDFLink("tool_center_point");
        return new URDFCartesian(
                "Cartesian",
                List.of(
                        base,
                        gantry,
                        head_stock,
                        spindle,
                        tool_center_point),
                List.of(
                        new URDFJoint(
                                "base_gantry",
                                JointType.prismatic,
                                new Limit(1000, 0, 1, 1),
                                base,
                                gantry,
                                new Pose3d(),
                                VecBuilder.fill(0, 1, 0)),
                        new URDFJoint(
                                "gantry_head",
                                JointType.prismatic,
                                new Limit(1000, 0, 1, 1),
                                gantry,
                                head_stock,
                                new Pose3d(),
                                VecBuilder.fill(1, 0, 0)),
                        new URDFJoint(
                                "head_spindle",
                                JointType.prismatic,
                                new Limit(1000, 0, 0.2, 1),
                                head_stock,
                                spindle,
                                new Pose3d(),
                                VecBuilder.fill(0, 0, 1)),
                        new URDFJoint(
                                "center_point",
                                JointType.fixed,
                                null,
                                spindle,
                                tool_center_point,
                                new Pose3d(0, 0, -0.1, new Rotation3d()),
                                null)));
    }
}
