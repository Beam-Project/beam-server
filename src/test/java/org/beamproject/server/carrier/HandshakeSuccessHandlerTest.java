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
package org.beamproject.server.carrier;

import org.beamproject.common.Message;
import org.beamproject.common.MessageField;
import static org.beamproject.common.MessageField.ContentField.HSNONCE;
import static org.beamproject.common.MessageField.ContentField.HSPUBKEY;
import static org.beamproject.common.MessageField.ContentField.HSSIG;
import static org.beamproject.common.MessageField.ContentField.TYP;
import static org.beamproject.common.MessageField.ContentField.TypeValue.HS_RESPONSE;
import org.beamproject.common.Participant;
import org.beamproject.common.carrier.MessageException;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.server.util.HandshakeStorage;
import org.beamproject.server.util.SessionStorage;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeSuccessHandlerTest {

    private HandshakeSuccessHandler handler;
    private HandshakeStorage<HandshakeResponder> handshakeStorage;
    private SessionStorage sessionStorage;
    private Participant user, server;
    private HandshakeChallenger challenger;
    private HandshakeResponder responder;
    private Message challenge, response, success, nullMessage;

    @Before
    public void setUp() {
        server = Participant.generate();
        user = Participant.generate();
        handshakeStorage = new HandshakeStorage<>();
        sessionStorage = new SessionStorage();

        challenger = new HandshakeChallenger(user);
        responder = new HandshakeResponder(server);
        handler = new HandshakeSuccessHandler(handshakeStorage, sessionStorage);

        setUpSuccess();
    }

    private void setUpSuccess() {
        challenge = challenger.produceChallenge(server);
        responder.consumeChallenge(challenge);
        response = responder.produceResponse();
        challenger.consumeResponse(response);
        success = challenger.produceSuccess();
    }

    @Test(expected = MessageException.class)
    public void testHandelOnMissingType() {
        success.getContent().remove(TYP.toString());
        nullMessage = handler.handle(success);
    }

    @Test(expected = MessageException.class)
    public void testHandelOnMissingPublicKey() {
        success.getContent().remove(HSPUBKEY.toString());
        nullMessage = handler.handle(success);
    }

    @Test(expected = MessageException.class)
    public void testHandelOnInvalidPublicKey() {
        success.putContent(HSPUBKEY, "not a public key".getBytes());
        nullMessage = handler.handle(success);
    }

    @Test(expected = MessageException.class)
    public void testHandelOnMissingSignature() {
        success.getContent().remove(HSSIG.toString());
        nullMessage = handler.handle(success);
    }

    @Test(expected = MessageException.class)
    public void testHandelOnInvalidSignature() {
        success.putContent(HSSIG, "not a signature".getBytes());
        nullMessage = handler.handle(success);
    }

    @Test(expected = MessageException.class)
    public void testHandelOnMissingHandshake() {
        nullMessage = handler.handle(success);
    }

    @Test
    public void testHandelOnStoringSessionAndDestroyingHandshake() {
        Participant userPublicOnly = new Participant(fromPublicKey(user.getPublicKeyAsBytes()));
        handshakeStorage.setHandshake(userPublicOnly, responder);
        nullMessage = handler.handle(success);

        assertNull(nullMessage);
        assertTrue(sessionStorage.isSessionExisting(responder.getSessionKey()));
        assertFalse(handshakeStorage.isHandshakeExisting(userPublicOnly));
    }

}
