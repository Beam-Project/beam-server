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

import javax.servlet.annotation.WebServlet;
import static org.beamproject.common.MessageField.*;
import org.beamproject.common.Participant;
import static org.beamproject.common.crypto.Handshake.*;
import org.beamproject.common.crypto.HandshakeException;
import org.beamproject.common.crypto.HandshakeResponse;
import org.beamproject.server.App;

/**
 * This servlet allows to establish authentication between a client and this
 * server.
 */
@WebServlet(urlPatterns = {"/authentication"})
public class AuthenticationPage extends Page {

    private static final long serialVersionUID = 1L;
    private Phase currentPhase;
    private HandshakeResponse handshakeResponse;

    @Override
    protected void processMessage() {
        Participant user = message.getRecipient();

        verifyEssentialFields();
        handshakeResponse = App.getModel().getHandshakeResponseByUser(user);

        switch (currentPhase) {
            case CHALLENGE:
                consumeChallengeAndResponse();
                break;
            case SUCCESS:
                consumeSuccess();
                storeUserToSessionKey();
                destroyHandshake();
                break;
            case FAILURE:
            default:
                destroyHandshake();
        }
    }

    private void verifyEssentialFields() {
        if (!message.containsContent(CNT_CRPHASE)) {
            throw new MessageException("The message does not contain the required field " + CNT_CRPHASE + ".");
        }

        try {
            currentPhase = Phase.valueOf(message.getContent(CNT_CRPHASE));
        } catch (IllegalArgumentException ex) {
            throw new MessageException("The given Phase is not valid: " + ex.getMessage());
        }
    }

    private void consumeChallengeAndResponse() {
        try {
            handshakeResponse.consumeChallenge(message);
            responseMessage = handshakeResponse.produceResponse();
        } catch (IllegalStateException | HandshakeException ex) {
            throw new MessageException("The message could not be processed: " + ex.getMessage());
        }
    }

    private void consumeSuccess() {
        try {
            handshakeResponse.consumeSuccess(message);
        } catch (IllegalStateException | HandshakeException ex) {
            throw new MessageException("The message could not be processed: " + ex.getMessage());
        }
    }

    private void storeUserToSessionKey() {
        App.getModel().addSession(handshakeResponse.getRemoteParticipant(), handshakeResponse.getSessionKey());
    }

    private void destroyHandshake() {
        App.getModel().destroyHandshakeResponseByUser(message.getRecipient());
    }

}
