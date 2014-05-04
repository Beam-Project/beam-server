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
import static org.beamproject.common.MessageField.ContentField.HSPHASE;
import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.Handshake.Phase;
import org.beamproject.common.crypto.HandshakeException;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.server.App;
import static org.beamproject.server.App.getModel;

/**
 * This {@link MessageHandler} allows to establish authentication between a user
 * respectively client and this server.
 */
public class HandshakeHandler implements MessageHandler {

    private Phase currentPhase;
    private HandshakeResponder responder;
    private Message message;
    private Page page;

    @Override
    public void handle(Message message, Page page) {
        verifyEssentialFields(message);
        this.message = message;
        this.page = page;
        this.responder = getModel().getHandshakeResponseByUser(message.getRecipient());

        switch (currentPhase) {
            case CHALLENGE:
                consumeChallengeAndProduceResponse();
                break;
            case SUCCESS:
                consumeSuccess();
                storeUserToSessionKey();
                destroyHandshake();
                break;
            case FAILURE:
            case INVALIDATE:
            default:
                System.out.println("Phase " + currentPhase + ": invalidate session");
                destroyHandshake();
        }
    }

    private void verifyEssentialFields(Message message) {
        if (!message.containsContent(HSPHASE)) {
            throw new MessageException("The message does not contain the required field " + HSPHASE + ".");
        }

        try {
            currentPhase = Handshake.Phase.valueOf(message.getContent(HSPHASE));
        } catch (IllegalArgumentException ex) {
            throw new MessageException("The given Phase is not valid: " + ex.getMessage());
        }
    }

    private void consumeChallengeAndProduceResponse() {
        try {
            responder.consumeChallenge(message);
            page.setResponseMessage(responder.produceResponse());
        } catch (IllegalStateException | HandshakeException ex) {
            destroyHandshake();
            throw new MessageException("The message could not be processed: " + ex.getMessage());
        }
    }

    private void consumeSuccess() {
        try {
            responder.consumeSuccess(message);
        } catch (IllegalStateException | HandshakeException ex) {
            destroyHandshake();
            throw new MessageException("The message could not be processed: " + ex.getMessage()
            );
        }
    }

    private void storeUserToSessionKey() {
        App.getModel().addSession(responder.getRemoteParticipant(), responder.getSessionKey());
    }

    private void destroyHandshake() {
        App.getModel().destroyHandshakeResponseByUser(message.getRecipient());
    }
}
