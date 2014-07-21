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

import org.beamproject.server.model.CarrierModel;
import org.beamproject.common.Participant;

/**
 * A {@link Carrier} is like a postal service (respectively a <i>carrier of
 * mail</i>) in real life; one places a letter at the post office and they send
 * it to its destination. We can also receive letters delivered by a carrier.
 * Implementations of {@link Carrier} do the same, but probably via different
 * media or protocols (MQTT, HTTP, pneumatic post, carrier pigeon, etc.).
 * <p>
 * When sending a message using a {@link Carrier}, the recipient (a
 * {@link Participant}) and the message (a byte array) have to be provided. The
 * {@link Carrier} tries then to deliver the message to its destination.
 * <p>
 * Please note, that a {@link Carrier} (hopefully like the local post office)
 * only knows the recipient with its address and possesses the message, but
 * never sees the content of the message. Therefore, no {@link Carrier} does any
 * cryptographic operations.
 *
 * @param <T> The type of the Carrier, either {@link ClientCarrier} or
 * {@link ServerCarrier}.
 * @see ClientCarrier
 * @see ServerCarrier
 */
public interface Carrier<T extends CarrierModel> {

    /**
     * Delivers the given message to the recipient.
     *
     * @param message The message to send. This has to be already encrypted.
     * @param recipient The recipient of the message.
     */
    public void deliverMessage(byte[] message, Participant recipient);

    /**
     * Start to receive messages.
     */
    public void startReceiving();

    /**
     * Takes received messages and redirects them to the configured
     * {@link CarrierModel}. This may only be invoked after
     * {@code this.startReceiving()} was successfully invoked.
     *
     * @param message The new message to handle.
     */
    public void receive(byte[] message);

    /**
     * Do not receive further messages.
     */
    public void stopReceiving();

    /**
     * Stop receiving further incoming messages and close all connections.
     */
    public void shutdown();
}
