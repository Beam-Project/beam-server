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

import static java.net.HttpURLConnection.*;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.server.App;
import org.beamproject.common.Session;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.junit.Before;

public class AuthenticationPageTest extends PageTest {

    private AuthenticationPage page;
    private HandshakeChallenger challenger;

    @Before
    public void setDeliveryPageUp() {
        page = new AuthenticationPage();
        basicSetup(page);
        challenger = new HandshakeChallenger(user);
    }

    @Test
    public void testChallengeOnMissingPhase() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(CRPHASE.toString());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testChallengeOnMissingNonce() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(CRNONCE.toString());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testChallengeOnMissingPublicKey() {
        message = challenger.produceChallenge(server);
        message.getContent().remove(CRPUBKEY.toString());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testChallengeOnTripleRequest() {
        message = challenger.produceChallenge(server);
        setMessageToRequest();

        // Okay, first challenge.
        sendRequestAndExtractResponseToMessage();

        // Second challenge -> destroy Handshake on server side.
        sendRequestAndCatchException(HTTP_BAD_REQUEST);

        // Third challenge is as if it was the first one.
        sendRequestAndExtractResponseToMessage();
    }

    @Test
    public void testOnDoubleRequest() {
        Message original = challenger.produceChallenge(server);
        message = original;
        setMessageToRequest();
        sendRequestAndExtractResponseToMessage();

        message = original;
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnCompleteCyclus() {
        message = challenger.produceChallenge(server);
        setMessageToRequest();
        sendRequestAndExtractResponseToMessage();

        challenger.consumeResponse(message);
        message = challenger.produceSuccess();
        setMessageToRequest();
        sendRequestAndExtractResponseToMessage();

        Session sessionFromModel = App.getModel().getSessionByKey(challenger.getSessionKey());
        assertArrayEquals(challenger.getSessionKey(), sessionFromModel.getKey());
    }

}
