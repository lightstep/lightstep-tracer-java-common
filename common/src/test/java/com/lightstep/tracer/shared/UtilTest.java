package com.lightstep.tracer.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest {
    @Test
    public void testToAndFromHexString() {
        assertEquals(1, Util.fromHexString("1"));
        assertEquals(-1, Util.fromHexString(Util.toHexString(-1)));
        assertEquals(Long.MAX_VALUE, Util.fromHexString(Util.toHexString(Long.MAX_VALUE)));
        assertEquals(Long.MIN_VALUE, Util.fromHexString(Util.toHexString(Long.MIN_VALUE)));
        assertEquals(0, Util.fromHexString(Util.toHexString(0)));
        long randomLong = Util.generateRandomGUID();
        assertEquals(randomLong, Util.fromHexString(Util.toHexString(randomLong)));
    }
}
