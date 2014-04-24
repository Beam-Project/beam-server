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
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.HandshakeResponse;
import org.beamproject.common.crypto.KeyPairCryptor;
import org.beamproject.common.util.ConfigWriter;
import static org.beamproject.server.App.getConfig;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ModelTest {

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

        HandshakeResponse handshake = model.getHandshakeResponseByUser(user);

        assertTrue(model.activeHandshakes.containsKey(user));
        assertEquals(handshake, model.activeHandshakes.get(user));
    }

    @Test
    public void testGetHandshakeResponseByUserOnExistingHandshake() {
        model.server = Participant.generate();
        Participant user = Participant.generate();
        HandshakeResponse handshakeResponse = new HandshakeResponse(model.server);
        model.activeHandshakes.put(user, handshakeResponse);

        assertEquals(1, model.activeHandshakes.size());
        assertEquals(handshakeResponse, model.activeHandshakes.get(user));
        assertEquals(1, model.activeHandshakes.size());
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
