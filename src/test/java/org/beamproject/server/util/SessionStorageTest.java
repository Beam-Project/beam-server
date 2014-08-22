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

import org.beamproject.common.Session;
import org.beamproject.common.User;
import org.beamproject.common.util.ComparableBytes;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class SessionStorageTest {

    private final User USER = User.generate();
    private final byte[] KEY = "my key".getBytes();
    private final Session SESSION = new Session(USER, KEY);
    private SessionStorage storage;

    @Before
    public void setUp() {
        storage = new SessionStorage();
    }

    @Test
    public void testIsSessionExisting() {
        assertFalse(storage.isSessionExisting(KEY));
        assertFalse(storage.isSessionExisting(null));

        storage.sessions.put(new ComparableBytes(KEY), SESSION);

        assertTrue(storage.isSessionExisting(KEY));
        assertFalse(storage.isSessionExisting(null));
        assertFalse(storage.isSessionExisting("other key".getBytes()));
    }

    @Test
    public void testSetAndGetSession() {
        storage.setSession(USER, KEY);

        assertTrue(storage.sessions.containsKey(new ComparableBytes(KEY)));
        assertEquals(USER, storage.getSessionByKey(KEY).getRemoteParticipant());
        assertArrayEquals(KEY, storage.getSessionByKey(KEY).getKey());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionOnMissingEntry() {
        storage.getSessionByKey(KEY);
    }

    @Test
    public void testRemoveSession() {
        storage.setSession(USER, KEY);
        Session session = storage.getSessionByKey(KEY);

        storage.removeSession(KEY);

        assertFalse(storage.isSessionExisting(KEY));
        
        for (byte b : session.getKey()) {
            assertEquals((byte) 0, b);
        }
    }

}
