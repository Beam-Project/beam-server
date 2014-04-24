/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-server.
 *
 * beam-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.server.util;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ComparableBytesTest {

    private ComparableBytes comparableBytes;
    private final byte[] bytes = "hello".getBytes();
    private final byte[] otherBytes = "hello2".getBytes();

    @Before
    public void setUp() {

    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNull() {
        comparableBytes = new ComparableBytes(null);
    }

    @Test
    public void testConstructorOnAssignment() {
        comparableBytes = new ComparableBytes(bytes);
        assertSame(bytes, comparableBytes.bytes);
    }

    @Test
    public void testGetBytes() {
        comparableBytes = new ComparableBytes(bytes);
        assertSame(bytes, comparableBytes.getBytes());
    }

    @Test
    public void testEquals() {
        comparableBytes = new ComparableBytes(bytes);
        ComparableBytes other = null;
        assertFalse(comparableBytes.equals(null));
        assertFalse(comparableBytes.equals(other));

        other = new ComparableBytes(otherBytes);
        assertFalse(comparableBytes.equals(other));

        other.bytes = bytes;
        assertTrue(comparableBytes.equals(other));

        other = new ComparableBytes(bytes);
        assertTrue(comparableBytes.equals(other));
        assertTrue(comparableBytes.equals(comparableBytes));
    }

}
