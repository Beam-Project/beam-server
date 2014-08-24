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
package org.beamproject.server.model;

import com.google.inject.Inject;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import lombok.experimental.Delegate;
import org.beamproject.common.carrier.ClientCarrier;
import static org.beamproject.common.carrier.ClientCarrier.MQTT_OUT_TOPIC_PREFIX;
import org.beamproject.common.carrier.ClientCarrierModel;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.crypto.CryptoPackerPool;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.common.message.Message;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;
import org.beamproject.server.carrier.HandshakeChallengeHandler;
import org.beamproject.server.carrier.HandshakeSuccessHandler;
import org.beamproject.server.util.HandshakeStorage;
import org.beamproject.server.util.SessionStorage;

/**
 * This class implements the {@link ClientCarrierModel} interface and therefore
 * provides functionality to control a {@link ClientCarrier}.
 */
public class ClientCarrierModelImpl implements ClientCarrierModel {

    @Inject
    Logger log;
    private final MainModel model;
    private final Executor executor;
    private final CryptoPackerPool packerPool;
    @Delegate
    private final ClientCarrier carrier;
    private final HandshakeStorage<HandshakeResponder> handshakeStorage;
    private final SessionStorage sessionStorage;

    @Inject
    public ClientCarrierModelImpl(MainModel model, Executor executor, ClientCarrier carrier,
            CryptoPackerPool packerPool, HandshakeStorage<HandshakeResponder> handshakeStorage,
            SessionStorage sessionStorage) {
        this.model = model;
        this.executor = executor;
        this.carrier = carrier;
        this.packerPool = packerPool;
        this.handshakeStorage = handshakeStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void consumeMessage(final byte[] ciphertext, final String username) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                CryptoPacker packer = null;

                try {
                    packer = packerPool.borrowObject();
                    Message message = packer.decryptAndUnpack(ciphertext, model.getServer());
                    Message response = produceResponse(message);

                    if (response != null) {
                        encryptAndSend(response, MQTT_OUT_TOPIC_PREFIX + username);
                    }
                } catch (Exception ex) {
                    log.log(WARNING, "Could not handle an incoming message: {0}", ex.getMessage());
                } finally {
                    packerPool.returnObject(packer);
                }
            }
        });
    }

    private Message produceResponse(Message message) {
        switch (message.getType()) {

            case HS_CHALLENGE:
                HandshakeChallengeHandler challengeHandler = new HandshakeChallengeHandler(handshakeStorage);
                return challengeHandler.handle(message);

            case HS_SUCCESS:
                HandshakeSuccessHandler successHandler = new HandshakeSuccessHandler(handshakeStorage, sessionStorage);
                return successHandler.handle(message);

            default:
                log.log(INFO, "Received a message of unknown type - ignore it.");
        }

        return null;
    }

    @Override
    public void encryptAndSend(final Message message, final String topic) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                CryptoPacker packer = null;

                try {
                    packer = packerPool.borrowObject();
                    byte[] ciphertext = packer.packAndEncrypt(message);
                    carrier.deliverMessage(ciphertext, topic);
                } catch (Exception ex) {
                    log.log(WARNING, "Could not send a message: {0}", ex.getMessage());
                } finally {
                    packerPool.returnObject(packer);
                }
            }
        });
    }

}
