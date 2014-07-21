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

import org.beamproject.common.User;
import org.beamproject.server.ExecutorFake;
import org.beamproject.server.model.ClientCarrierModel;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ClientCarrierImplTest {

    private final User USER = User.generate();
    private final String TOPIC = "myTopic";
    private final byte[] MESSAGE = "myMessage".getBytes();
    private ExecutorFake executorFake;
    private MqttConnectionPool connectionPool;
    private MqttConnection connection;
    private ClientCarrierModel model;
    private ClientCarrierImpl carrier;

    @Before
    public void setUp() {
        executorFake = new ExecutorFake();
        connectionPool = createMock(MqttConnectionPool.class);
        connection = createMock(MqttConnection.class);
        model = createMock(ClientCarrierModel.class);
        carrier = new ClientCarrierImpl(model, executorFake, connectionPool);
    }

    @Test
    public void testBindUserToTopic() {
        carrier.bindUserToTopic(USER, TOPIC);

        assertEquals(TOPIC, carrier.topics.get(USER));
    }

    @Test
    public void testUnbindUser() {
        carrier.topics.put(USER, TOPIC);

        carrier.unbindUser(USER);

        assertFalse(carrier.topics.contains(USER));
    }

    @Test(expected = CarrierException.class)
    public void testDeliverMessageOnMissingRecipientBinding() {
        carrier.deliverMessage(MESSAGE, USER);
    }

    @Test
    public void testDeliverMessage() throws Exception {
        carrier.bindUserToTopic(USER, TOPIC);
        expect(connectionPool.borrowObject()).andReturn(connection);
        connection.publish(TOPIC, MESSAGE);
        expectLastCall();
        connectionPool.returnObject(connection);
        replay(connectionPool, connection);

        carrier.deliverMessage(MESSAGE, USER);

        verify(connectionPool, connection);
    }

    @Test
    public void testStartReceiving() throws Exception {
        expect(connectionPool.borrowObject()).andReturn(connection);
        connection.subscribe(carrier);
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                assertSame(connection, carrier.subscriberConnection);
                return null;
            }
        });
        connectionPool.returnObject(connection);
        expectLastCall();
        replay(connectionPool, connection);

        assertNull(carrier.subscriberConnection);

        carrier.startReceiving();

        assertNull(carrier.subscriberConnection);
        verify(connectionPool, connection);
    }

    @Test(expected = IllegalStateException.class)
    public void testStopReceivingWhenNotReceiving() {
        carrier.stopReceiving();
    }

    @Test
    public void testStopReceiving() {
        carrier.subscriberConnection = connection;
        connection.doReceive(false);
        expectLastCall();
        replay(connection);

        carrier.stopReceiving();

        verify(connection);
    }

    @Test
    public void testReceive() {
        model.consumeMessage(MESSAGE);
        expectLastCall();
        replay(model);

        carrier.receive(MESSAGE);

        verify(model);
    }

    @Test
    public void testShutdown() {
        carrier.subscriberConnection = connection;
        
        connectionPool.close();
        expectLastCall();
        replay(connectionPool);

        carrier.shutdown();

        verify(connectionPool);
    }

}
