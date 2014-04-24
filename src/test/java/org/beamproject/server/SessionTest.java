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
package org.beamproject.server;

import org.beamproject.common.Participant;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class SessionTest {

    private Participant user;
    private Session session;
    private final byte[] key = "my secret session key".getBytes();

    @Before
    public void setUp() {
        user = Participant.generate();
        session = new Session(user, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        session = new Session(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUser() {
        session = new Session(null, key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullKey() {
        session = new Session(user, null);
    }

    @Test
    public void testConstructorOnAssignment() {
        assertSame(user, session.user);
        assertSame(key, session.key);
    }

    @Test
    public void testConstructorOnCreatingTimestamp() {
        assertNotNull(session.creationTimestamp);
    }

    @Test
    public void testGetUser() {
        assertSame(user, session.getUser());
        session.user = null;
        assertNull(session.getUser());
    }

    @Test
    public void testGetKey() {
        assertSame(key, session.getKey());
        session.key = null;
        assertNull(session.getKey());
    }

}
