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

import org.beamproject.common.Participant;
import org.beamproject.common.carrier.MessageException;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.HandshakeException;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.common.message.ContentFieldValidator;
import static org.beamproject.common.message.Field.Cnt.NONCE;
import static org.beamproject.common.message.Field.Cnt.PUBLIC_KEY;
import static org.beamproject.common.message.Field.Cnt.TYP;
import org.beamproject.common.message.HandshakeNonceValidator;
import org.beamproject.common.message.HandshakePublicKeyValidator;
import org.beamproject.common.message.Message;
import org.beamproject.common.message.MessageHandler;
import org.beamproject.server.util.HandshakeStorage;

/**
 * This {@link MessageHandler} is part of the {@link Handshake} procedure.
 * <p>
 * Handles messages of type: {@link TypeValue#HS_CHALLENGE}
 */
public class HandshakeChallengeHandler extends MessageHandler {

    private final HandshakeStorage<HandshakeResponder> handshakeStorage;
    private HandshakeResponder handshake;
    private Participant remoteParticipant;

    public HandshakeChallengeHandler(HandshakeStorage<HandshakeResponder> responders) {
        super(new ContentFieldValidator(TYP, NONCE, PUBLIC_KEY),
                new HandshakeNonceValidator(),
                new HandshakePublicKeyValidator());
        this.handshakeStorage = responders;
    }

    @Override
    protected Message handleValidMessage() {
        restoreRemoteParticipant();
        createHandshake();
        return consumeChallengeAndProduceResponse();
    }

    private void restoreRemoteParticipant() {
        try {
            byte[] remotePublicKeyBytes = message.getContent(PUBLIC_KEY);
            remoteParticipant = new Participant(fromPublicKey(remotePublicKeyBytes));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new MessageException("Could not restore the remote participant's public key: " + ex.getMessage());
        }
    }

    private void createHandshake() {
        handshake = new HandshakeResponder(message.getRecipient());
        handshakeStorage.setHandshake(remoteParticipant, handshake);
    }

    private Message consumeChallengeAndProduceResponse() {
        try {
            handshake.consumeChallenge(message);
            return handshake.produceResponse();
        } catch (IllegalStateException | HandshakeException ex) {
            destroyHandshake();
            throw new MessageException("The message could not be processed: " + ex.getMessage());
        }
    }

    private void destroyHandshake() {
        handshakeStorage.removeHandshake(remoteParticipant);
    }
}
