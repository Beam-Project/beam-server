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

import com.google.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import org.beamproject.server.model.ClientCarrierModel;
import org.beamproject.common.Participant;
import org.beamproject.common.User;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;

/**
 * Implements the {@link ClientCarrier} interface using <a
 * href="http://mqtt.org/">MQTT</a> to communicate with the clients.
 *
 * @see ClientCarrier
 * @see ClientCarrierModel
 */
public class ClientCarrierImpl implements ClientCarrier {

    private final ClientCarrierModel model;
    private final Executor executor;
    private final MqttConnectionPool connectionPool;
    ConcurrentHashMap<Participant, String> topics;
    MqttConnection subscriberConnection;

    @Inject
    public ClientCarrierImpl(ClientCarrierModel model, Executor executor, MqttConnectionPool connectionPool) {
        this.model = model;
        this.executor = executor;
        this.connectionPool = connectionPool;

        topics = new ConcurrentHashMap<>();
    }

    @Override
    public void bindUserToTopic(User user, String topic) {
        topics.put(user, topic);
    }

    @Override
    public void unbindUser(User user) {
        topics.remove(user);
    }

    /**
     * Delivers the given message to the recipient.
     *
     * @param message The message to send. This has to be already encrypted.
     * @param recipient The recipient of the message.
     * @throws CarrierException If the recipient is not bound to a topic or the
     * message could not be sent.
     */
    @Override
    public void deliverMessage(final byte[] message, final Participant recipient) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                if (!topics.containsKey(recipient)) {
                    throw new CarrierException("The recipiant is not mapped to a topic.");
                }

                try {
                    MqttConnection connection = connectionPool.borrowObject();
                    connection.publish(topics.get(recipient), message);
                    connectionPool.returnObject(connection);
                } catch (Exception ex) {
                    throw new CarrierException("The message could not be sent: " + ex.getMessage());
                }
            }
        });
    }

    /**
     * Start to receive messages and therefore subscribes this
     * {@link ClientCarrier}.
     *
     * @throws CarrierException If the subscription was not successful.
     */
    @Override
    public void startReceiving() {
        final ClientCarrier thisCarrier = this;

        executor.runAsync(new Task() {
            @Override
            public void run() {
                try {
                    subscriberConnection = connectionPool.borrowObject();
                    subscriberConnection.subscribe(thisCarrier);

                    connectionPool.returnObject(subscriberConnection);
                    subscriberConnection = null;
                } catch (Exception ex) {
                    throw new CarrierException("Could not subscribe to topic: " + ex.getMessage());
                }
            }
        });
    }

    @Override
    public void receive(byte[] message) {
        model.consumeMessage(message);
    }

    /**
     * Do not receive further messages.
     *
     * @throws IllegalStateException If this {@link ClientCarrier} was not
     * receiving before.
     */
    @Override
    public void stopReceiving() {
        if (subscriberConnection == null) {
            throw new IllegalStateException("This may only be invoked when receiving.");
        }

        subscriberConnection.doReceive(false);
    }

    @Override
    public void shutdown() {
        if (subscriberConnection != null) {
            stopReceiving();
        }

        connectionPool.close();
    }

}
