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

import org.beamproject.common.Session;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.common.crypto.KeyPairCryptor;
import org.beamproject.common.util.ConfigWriter;
import static org.beamproject.server.App.getConfig;
import org.beamproject.server.util.ComparableBytes;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ModelTest {

    private final byte[] SESSION_KEY = "my key".getBytes();
    private ConfigWriter writer;
    private Model model;

    @Before
    public void setUp() {
        ConfigTest.loadDefaultConfig();
        writer = createMock(ConfigWriter.class);
        AppTest.setAppConfigWriter(writer);
        model = new Model();
    }

    @Test
    public void testConstructor() {
        assertNotNull(model.activeHandshakes);
        assertNotNull(model.activeSessions);
    }

    @Test
    public void testGetServerWhenNotExisting() {
        writer.writeConfig(getConfig(), Config.FOLDER, Config.FILE);
        expectLastCall();
        replay(writer);

        App.getConfig().removeProperty("encryptedServerPublicKey");
        assertNull(model.server);
        assertNotNull(model.getServer());
        assertNotNull(model.server);
        assertSame(model.server, model.getServer());

        verify(writer);
    }

    @Test
    public void testGetServerWhenExistingInConfig() {
        Participant server = Participant.generate();
        EncryptedKeyPair encryptedKeyPair = KeyPairCryptor.encrypt(getConfig().keyPairPassword(), server.getKeyPair());
        getConfig().setProperty("keyPairSalt", encryptedKeyPair.getSalt());
        getConfig().setProperty("encryptedPublicKey", encryptedKeyPair.getEncryptedPublicKey());
        getConfig().setProperty("encryptedPrivateKey", encryptedKeyPair.getEncryptedPrivateKey());

        assertNull(model.server);
        assertEquals(server, model.getServer());
        assertEquals(server, model.server);
    }

    @Test
    public void testIsEncryptedKeyPairStored() {
        assertFalse(model.isEncryptedKeyPairStored());
        getConfig().setProperty("encryptedPublicKey", "pubkey");
        assertFalse(model.isEncryptedKeyPairStored());
        getConfig().setProperty("encryptedPrivateKey", "privkey");
        assertTrue(model.isEncryptedKeyPairStored());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetHandshakeResponseByUserOnNull() {
        model.getHandshakeResponseByUser(null);
    }

    @Test
    public void testGetHandshakeResponseByUserOnNewHandshake() {
        model.server = Participant.generate();
        Participant user = Participant.generate();

        assertFalse(model.activeHandshakes.contains(user));
        HandshakeResponder handshake = model.getHandshakeResponseByUser(user);
        assertSame(handshake, model.activeHandshakes.get(user));
        assertSame(handshake, model.getHandshakeResponseByUser(user));
    }

    @Test
    public void testGetHandshakeResponseByUserOnExistingHandshake() {
        model.server = Participant.generate();
        Participant user = Participant.generate();
        HandshakeResponder handshakeResponder = new HandshakeResponder(model.server);
        model.activeHandshakes.put(user, handshakeResponder);

        assertEquals(1, model.activeHandshakes.size());
        assertEquals(handshakeResponder, model.getHandshakeResponseByUser(user));
        assertEquals(1, model.activeHandshakes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestroyHandshakeResponseByUserOnNull() {
        model.destroyHandshakeResponseByUser(null);
    }

    @Test
    public void testDestroyHandshakeResponseByUserOnNewHandshake() {
        Participant user = Participant.generate();

        assertFalse(model.activeHandshakes.containsKey(user));
        model.destroyHandshakeResponseByUser(user);
        assertFalse(model.activeHandshakes.containsKey(user));
    }

    @Test
    public void testDestroyHandshakeResponseByUserOnExistingHandshake() {
        model.server = Participant.generate();
        Participant user = Participant.generate();
        HandshakeResponder handshakeResponder = new HandshakeResponder(model.server);
        model.activeHandshakes.put(user, handshakeResponder);

        assertEquals(1, model.activeHandshakes.size());
        model.destroyHandshakeResponseByUser(user);
        assertEquals(0, model.activeHandshakes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSessionOnNulls() {
        model.addSession(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSessionOnNullUser() {
        model.addSession(null, SESSION_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddSessionOnNullSessionKey() {
        model.addSession(Participant.generate(), null);
    }

    @Test
    public void testAddSession() {
        Participant user = Participant.generate();

        assertFalse(model.activeSessions.containsKey(new ComparableBytes(SESSION_KEY)));
        model.addSession(user, SESSION_KEY);
        assertTrue(model.activeSessions.containsKey(new ComparableBytes(SESSION_KEY)));
        Session session = model.activeSessions.get(new ComparableBytes(SESSION_KEY));
        assertEquals(user, session.getRemoteParticipant());
        assertArrayEquals(SESSION_KEY, session.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSessionByKeyOnNull() {
        model.getSessionByKey(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSessionByKeyOnMissingSession() {
        model.getSessionByKey(SESSION_KEY);
    }

    @Test
    public void testGetSessionByKey() {
        Session session = new Session(Participant.generate(), SESSION_KEY);
        model.activeSessions.put(new ComparableBytes(SESSION_KEY), session);
        assertSame(session, model.getSessionByKey(SESSION_KEY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSessionExistingByKeyOnNull() {
        model.isSessionExistingByKey(null);
    }

    @Test
    public void testIsSessionExistingByKey() {
        assertFalse(model.isSessionExistingByKey(SESSION_KEY));
        model.addSession(Participant.generate(), SESSION_KEY);
        assertTrue(model.isSessionExistingByKey(SESSION_KEY));
    }

    /**
     * Sets the given server to the given {@link Model}. This can be used for
     * unit testing purposes.
     *
     * @param server The server to set. This can be null.
     * @param model The model on that should be set.
     */
    public static void setServer(Participant server, Model model) {
        model.server = server;
    }

}
