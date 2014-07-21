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

import org.beamproject.server.carrier.Carrier;
import org.beamproject.server.carrier.ClientCarrier;
import org.beamproject.server.carrier.ServerCarrier;

/**
 * The {@link CarrierModel}s contain the business logic of the {@link Carrier}s.
 *
 * @param <T> The type of a {@link CarrierModel} what has to be a subtype of
 * {@link Carrier}.
 *
 * @see ClientCarrier
 * @see ServerCarrier
 * @see ClientCarrierModel
 * @see ServerCarrierModel
 */
public interface CarrierModel<T extends Carrier> {

    /**
     * Prepares the {@link Carrier} of this model and starts listening/receiving
     * incoming messages.
     */
    public void startReceiving();

    /**
     * Stops the {@link Carrier} of this model receiving new messages.
     */
    public void stopReceiving();

    /**
     * Consumes messages that are being received by the internally used
     * {@link Carrier}.
     *
     * @param message The message as byte array.
     */
    public void consumeMessage(byte[] message);

    /**
     * Shuts the {@link Carrier} of this model down and closes all connections.
     */
    public void shutdown();
}
