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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.beamproject.common.util.Exceptions;
import org.beamproject.server.util.Config;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

/**
 * Allows to send byte arrays to recipients via <a
 * href="http://mqtt.org/">MQTT</a>.
 */
public class MqttConnection {

    private final MQTT mqtt;
    private final String host;
    private final int port;
    private final String username;
    private final String subscriberTopic;
    BlockingConnection connection;
    private boolean doReceive;
    private boolean isSubscribing;

    public MqttConnection(MQTT mqtt, String host, int port, String username, String subscriberTopic) {
        Exceptions.verifyArgumentsNotNull(mqtt, host, username, subscriberTopic);
        verifyPort(port);

        this.mqtt = mqtt;
        this.host = host;
        this.port = port;
        this.username = username;
        this.subscriberTopic = subscriberTopic;
    }

    private void verifyPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("The port is invalid.");
        }
    }

    /**
     * Connects to the configured MQTT broker. This has to be done before
     * messages may be published or topic subscribed to.
     *
     * @throws CarrierException If the connection could not be established.
     */
    public void connect() {
        try {
            mqtt.setHost(host, port);
            mqtt.setUserName(username);
            connection = mqtt.blockingConnection();
            connection.connect();
        } catch (Exception ex) {
            throw new CarrierException("Could not connect to broker: " + ex.getMessage());
        }
    }

    /**
     * Publishes a {@code message} to the given {@code topic}.
     *
     * @param topic The topic to publish to.
     * @param message The message to send.
     * @throws CarrierException If the message could not be published.
     */
    public void publish(String topic, byte[] message) {
        try {
            connection.publish(topic, message, QoS.AT_LEAST_ONCE, false);
        } catch (Exception ex) {
            throw new CarrierException("Could not publish the message: " + ex.getMessage());
        }
    }

    /**
     * Subscribes the given {@link ClientCarrier} to the configured topic (see
     * {@link Config}).
     * <p>
     * Incoming messages will be delivered to
     * {@link ClientCarrier.receive(byte[])}.
     * <p>
     * This method blocks until {@code this.doReceive(false)} has been invoked.
     *
     * @param carrier The carrier to subscribe.
     * @throws CarrierException If the subscription was not successful.
     */
    public void subscribe(ClientCarrier carrier) {
        try {
            Topic[] topics = new Topic[]{new Topic(subscriberTopic, QoS.AT_LEAST_ONCE)};
            connection.subscribe(topics);
            isSubscribing = true;
            doReceive(true);
        } catch (Exception ex) {
            doReceive(false);
            throw new CarrierException("Could not subscribe to topic '"
                    + subscriberTopic + "': " + ex.getMessage());
        }

        Message message;
        Logger logger = Logger.getLogger(MqttConnection.class.getName());

        while (doReceive) {
            try {
                message = connection.receive();
                carrier.receive(message.getPayload());
                message.ack();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Could not receive from topic: {0}", ex.getMessage());
            }
        }
    }

    /**
     * Allows to change the state of the internally used boolean whether new
     * messages should be received or not.
     * <p>
     * This has no effect if this connection is not subscribing at the moment of
     * invocation.
     * <p>
     * This method is synchronized.
     *
     * @param doReceive Whether or not this connection should receive messages.
     */
    synchronized public void doReceive(boolean doReceive) {
        this.doReceive = doReceive;
    }

    /**
     * Disconnects from the MQTT broker. If there is a subscription established,
     * it will be undone first.
     *
     * @throws CarrierException If the disconnection was not successful.
     */
    public void disconnect() {
        try {
            if (isSubscribing) {
                connection.unsubscribe(new String[]{subscriberTopic});
            }

            connection.disconnect();
        } catch (Exception ex) {
            throw new CarrierException("Could not disconnect: " + ex.getMessage());
        }
    }

}
