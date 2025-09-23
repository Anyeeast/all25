package org.team100.dynamics.pr;

/**
 * Joint configuration for the PR example.
 * 
 * @param q1 extension of the P joint (+x)
 * @param q2 rotation of the R joint (CCW from +x)
 */
public record Config(double q1, double q2) {

}
