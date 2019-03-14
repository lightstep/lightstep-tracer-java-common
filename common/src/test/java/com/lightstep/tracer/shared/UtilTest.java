package com.lightstep.tracer.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest {
    @Test
    public void testToAndFromHexString() {
        assertEquals(new Long(1), Util.fromHexString("1"));
        assertEquals(new Long(-1), Util.fromHexString(Util.toHexString(-1)));
        assertEquals(new Long(Long.MAX_VALUE), Util.fromHexString(Util.toHexString(Long.MAX_VALUE)));
        assertEquals(new Long(Long.MIN_VALUE), Util.fromHexString(Util.toHexString(Long.MIN_VALUE)));
        assertEquals(new Long(0), Util.fromHexString(Util.toHexString(0)));
        Long randomLong = Util.generateRandomGUID();
        assertEquals(randomLong, Util.fromHexString(Util.toHexString(randomLong)));

        assertEquals(null, Util.fromHexString(null));
        assertEquals(null, Util.fromHexString(""));
        assertEquals(null, Util.fromHexString("$#%"));
    }
}
