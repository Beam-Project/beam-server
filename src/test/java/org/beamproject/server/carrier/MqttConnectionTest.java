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

import static org.easymock.EasyMock.*;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class MqttConnectionTest {

    private final String HOST = "localhost";
    private final int PORT = 3625;
    private final String USERNAME = "secret_username";
    private final String SUBSCRIBER_TOPIC = "in";
    private MQTT mqtt;
    private MqttConnection connection;
    private BlockingConnection blockingConnection;

    @Before
    public void setUp() {
        mqtt = createMock(MQTT.class);
        blockingConnection = createMock(BlockingConnection.class);
        connection = new MqttConnection(mqtt, HOST, PORT, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNulls() {
        connection = new MqttConnection(null, null, 0, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullMqtt() {
        connection = new MqttConnection(null, HOST, PORT, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullHost() {
        connection = new MqttConnection(new MQTT(), null, PORT, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullUsername() {
        connection = new MqttConnection(new MQTT(), HOST, PORT, null, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNullSubscriberTopic() {
        connection = new MqttConnection(new MQTT(), HOST, PORT, USERNAME, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnNegativePort() {
        connection = new MqttConnection(new MQTT(), HOST, -1, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnZeroPort() {
        connection = new MqttConnection(new MQTT(), HOST, 0, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorOnTooHighPort() {
        connection = new MqttConnection(new MQTT(), HOST, 65536, USERNAME, SUBSCRIBER_TOPIC);
    }

    @Test
    public void testConnect() throws Exception {
        mqtt.setHost(HOST, PORT);
        expectLastCall();
        mqtt.setUserName(USERNAME);
        expectLastCall();
        expect(mqtt.blockingConnection()).andReturn(blockingConnection);
        blockingConnection.connect();
        expectLastCall();
        replay(mqtt, blockingConnection);

        connection.connect();

        assertSame(blockingConnection, connection.connection);
        verify(mqtt, blockingConnection);
    }

    @Test
    public void testPublish() throws Exception {
        String topic = "myTopic";
        byte[] message = "secret message".getBytes();
        connection.connection = blockingConnection;
        blockingConnection.publish(topic, message, QoS.AT_LEAST_ONCE, false);
        expectLastCall();
        replay(mqtt, blockingConnection);

        connection.publish(topic, message);

        verify(mqtt, blockingConnection);
    }

    @Test
    public void testDisconnect() throws Exception {
        connection.connection = blockingConnection;
        blockingConnection.disconnect();
        expectLastCall();
        replay(mqtt, blockingConnection);

        connection.disconnect();

        verify(mqtt, blockingConnection);
    }

}
