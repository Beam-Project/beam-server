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

import org.beamproject.common.User;
import org.beamproject.common.crypto.HandshakeResponder;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class HandshakeStorageTest {

    private final User USER = User.generate();
    private final HandshakeResponder handshake = new HandshakeResponder(USER);
    private HandshakeStorage<HandshakeResponder> storage;

    @Before
    public void setUp() {
        storage = new HandshakeStorage<>();
    }

    @Test
    public void testIsHandshakeExisting() {
        assertFalse(storage.isHandshakeExisting(USER));
        assertFalse(storage.isHandshakeExisting(null));

        storage.handshakes.put(USER, handshake);

        assertTrue(storage.isHandshakeExisting(USER));
        assertFalse(storage.isHandshakeExisting(null));
        assertFalse(storage.isHandshakeExisting(User.generate()));
    }

    @Test
    public void testSetAndGetHandshake() {
        storage.setHandshake(USER, handshake);

        assertTrue(storage.handshakes.containsKey(USER));
        assertSame(handshake, storage.getHandshake(USER));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetHandshakeOnMissingEntry() {
        storage.getHandshake(USER);
    }

    @Test
    public void testRemoveHandshake() {
        storage.setHandshake(USER, handshake);
        storage.removeHandshake(USER);

        assertFalse(storage.isHandshakeExisting(USER));
    }

}
