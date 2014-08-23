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

import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Field.Cnt.HS_NONCE;
import static org.beamproject.common.message.Field.Cnt.HS_PUBKEY;
import static org.beamproject.common.message.Field.Cnt.TYP;
import static org.beamproject.common.message.Field.Cnt.Typ.HS_RESPONSE;
import org.beamproject.common.Participant;
import org.beamproject.common.carrier.MessageException;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.server.util.HandshakeStorage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.Before;

public class HandshakeChallengeHandlerTest {

    private HandshakeChallengeHandler handler;
    private HandshakeStorage<HandshakeResponder> handshakeStorage;
    private Participant user, server;
    private HandshakeChallenger challenger;
    private Message challenge, response, success;

    @Before
    public void setUp() {
        server = Participant.generate();
        user = Participant.generate();
        handshakeStorage = new HandshakeStorage<>();

        challenger = new HandshakeChallenger(user);
        handler = new HandshakeChallengeHandler(handshakeStorage);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMissingType() {
        challenge = challenger.produceChallenge(server);
        challenge.getContent().remove(TYP.toString());
        response = handler.handle(challenge);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMissingNonce() {
        challenge = challenger.produceChallenge(server);
        challenge.getContent().remove(HS_NONCE.toString());
        response = handler.handle(challenge);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMissingPublicKey() {
        challenge = challenger.produceChallenge(server);
        challenge.getContent().remove(HS_PUBKEY.toString());
        response = handler.handle(challenge);
    }

    @Test
    public void testHandleOnCreatingHandshake() {
        challenge = challenger.produceChallenge(server);
        response = handler.handle(challenge);

        Participant userOnlyPublicKey = new Participant(fromPublicKey(user.getPublicKeyAsBytes()));
        assertTrue(handshakeStorage.isHandshakeExisting(userOnlyPublicKey));
        assertEquals(HS_RESPONSE, response.getType());
        challenger.consumeResponse(response);
        challenger.produceSuccess(); // expect no exception to be thrown        
    }

}
