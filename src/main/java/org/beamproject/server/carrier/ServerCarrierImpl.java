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

import org.beamproject.common.carrier.CarrierException;
import org.beamproject.common.carrier.ServerCarrier;
import com.google.inject.Inject;
import org.beamproject.common.carrier.ServerCarrierModel;
import org.beamproject.common.Participant;
import org.beamproject.common.Server;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;

/**
 * Implements the {@link ServerCarrier} interface using HTTP to communicate with
 * other servers of the Beam network.
 *
 * @see ServerCarrier
 * @see ServerCarrierModel
 */
public class ServerCarrierImpl implements ServerCarrier {

    private final ServerCarrierModel model;
    private final Executor executor;
    private final HttpConnectionPool connectionPool;
    private final HttpServer httpServer;

    @Inject
    public ServerCarrierImpl(ServerCarrierModel model,
            Executor executor,
            HttpConnectionPool connectionPool,
            HttpServer httpServer) {
        this.model = model;
        this.executor = executor;
        this.connectionPool = connectionPool;
        this.httpServer = httpServer;
    }

    /**
     * Delivers the given message to the recipient.
     *
     * @param message The message to send. This has to be already encrypted.
     * @param recipient The recipient of the message.
     * @throws IllegalArgumentException If the given recipient is not of a
     * {@link Server}.
     * @throws CarrierException If the message could not be sent.
     */
    @Override
    public void deliverMessage(final byte[] message, final Participant recipient) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                if (!(recipient instanceof Server)) {
                    throw new IllegalArgumentException("The given recipient is not a Server.");
                }

                Server targetServer = (Server) recipient;

                try {
                    HttpConnection connection = connectionPool.borrowObject();
                    connection.post(targetServer.getHttpUrl(), message);
                    connectionPool.returnObject(connection);
                } catch (Exception ex) {
                    throw new CarrierException("The message could not be sent:" + ex.getMessage());
                }
            }
        });
    }

    @Override
    public void startReceiving() {
        httpServer.setCarrier(this);
        httpServer.start();
    }

    @Override
    public void stopReceiving() {
        httpServer.stop();
    }

    @Override
    public void receive(byte[] message) {
        model.consumeMessage(message);
    }

    @Override
    public void shutdown() {
        connectionPool.close();
        httpServer.stop();
    }

}
