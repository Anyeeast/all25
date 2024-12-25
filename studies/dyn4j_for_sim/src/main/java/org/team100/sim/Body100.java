package org.team100.sim;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.listener.StepListener;

public abstract class Body100 extends Body implements StepListener<Body100>, Filter {

    protected final String m_id;
    private final boolean m_debug;

    /**
     * This is the list of types that will be rendered.
     * 
     * We don't try to render the {@link Wall} type because simgui can't do it
     * correctly.
     */
    public static List<Class<? extends Body100>> types() {
        return List.of(
                Friend.class,
                Foe.class,
                Note.class,
                Obstacle.class,
                Player.class);
    }

    private static final Set<String> ids = new HashSet<>();

    protected Body100(String id, boolean debug) {
        if (ids.contains(id))
            throw new IllegalArgumentException("duplicate id: " + id);
        m_id = id;
        m_debug = debug;
        setUserData(id);
    }

    @Override
    public void begin(TimeStep step, PhysicsWorld<Body100, ?> world) {
        //
    }

    @Override
    public void updatePerformed(TimeStep step, PhysicsWorld<Body100, ?> world) {
        //
    }

    @Override
    public void postSolve(TimeStep step, PhysicsWorld<Body100, ?> world) {
        //
    }

    @Override
    public void end(TimeStep step, PhysicsWorld<Body100, ?> world) {
        //
    }

    /**
     * Filter based on vertical extent.
     * Since the filters can be called in either order, there's just one filter for
     * all the cases, so this is final.
     */
    @Override
    public final boolean isAllowed(Filter filter) {
        if (!(filter instanceof Body100)) {
            // this should never happen
            return true;
        }
        return getVerticalExtent().overlaps(((Body100) filter).getVerticalExtent());
    }

    /**
     * Vertical extent can vary (e.g. for notes), or be fixed (for everything else).
     */
    public abstract Range getVerticalExtent();

    @Override
    public String toString() {
        return String.format("Body100 [%s]", m_id);
    }

    public boolean isDebug() {
        return m_debug;
    }

}
