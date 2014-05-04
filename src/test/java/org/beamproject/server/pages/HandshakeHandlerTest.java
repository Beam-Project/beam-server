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
package org.beamproject.server.pages;

import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.HSNONCE;
import static org.beamproject.common.MessageField.ContentField.HSPHASE;
import static org.beamproject.common.MessageField.ContentField.HSPUBKEY;
import org.beamproject.common.Participant;
import org.beamproject.common.Session;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.server.App;
import org.beamproject.server.AppTest;
import org.beamproject.server.Model;
import org.beamproject.server.ModelTest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HandshakeHandlerTest {

    private Model model;
    private Page page;
    private HandshakeHandler handler;
    private Participant user, server;
    private HandshakeChallenger challenger;
    private Message message;

    @Before
    public void setUp() {
        server = Participant.generate();
        user = Participant.generate();
        model = new Model();
        ModelTest.setServer(server, model);
        AppTest.setAppModel(model);

        challenger = new HandshakeChallenger(user);
        handler = new HandshakeHandler();
        page = new Page();
    }

    @Test(expected = MessageException.class)
    public void testChallengeOnMissingPhase() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(HSPHASE.toString());
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testChallengeOnMissingNonce() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(HSNONCE.toString());
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testChallengeOnMissingPublicKey() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(HSPUBKEY.toString());
        handler.handle(message, page);
    }

    @Test
    public void testChallengeOnTripleRequest() {
        message = challenger.produceChallenge(server);

        // First challenge -> should work
        handler.handle(message, page);

        // Second challenge -> destroy Handshake on server side.
        try {
            handler.handle(message, page);
            fail("An exception should have been thrown.");
        } catch (MessageException ex) {
        }

        // Third challenge is as if it was the first one.
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testOnDoubleRequest() {
        Message original = challenger.produceChallenge(server);
        message = original;
        handler.handle(message, page);

        message = original;
        handler.handle(message, page);
    }

    @Test
    public void testOnCompleteCyclus() {
        message = challenger.produceChallenge(server);
        handler.handle(message, page);

        challenger.consumeResponse(page.responseMessage);
        message = challenger.produceSuccess();
        handler.handle(message, page);

        Session sessionFromModel = App.getModel().getSessionByKey(challenger.getSessionKey());
        assertArrayEquals(challenger.getSessionKey(), sessionFromModel.getKey());
    }

}
