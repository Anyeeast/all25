package org.team100.lib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.interpolation.Interpolatable;

class TimeInterpolatableBuffer100Test {
    private static final double DELTA = 0.001;

    static class Item implements Interpolatable<Item> {
        public final double value;

        public Item(double v) {
            value = v;
        }

        @Override
        public Item interpolate(Item endValue, double t) {
            return new Item(MathUtil.interpolate(value, endValue.value, t));
        }
    }

    /** It interpolates proportionally. */
    @Test
    void testSimple2() {
        TimeInterpolatableBuffer100<Item> b = new TimeInterpolatableBuffer100<>(10, 0, new Item(0));
        assertEquals(0, b.get(0).value, DELTA);
        b.put(1, new Item(10));
        assertEquals(5, b.get(0.5).value, DELTA);
        assertEquals(7.5, b.get(0.75).value, DELTA);
    }

    /** For off-the-end requests, it returns the last item. */
    @Test
    void testOffTheEnd2() {
        TimeInterpolatableBuffer100<Item> b = new TimeInterpolatableBuffer100<>(10, 0, new Item(0));
        assertEquals(0, b.get(1).value, DELTA);
        b.put(1, new Item(10));
        assertEquals(10, b.get(1.5).value, DELTA);
    }

}
