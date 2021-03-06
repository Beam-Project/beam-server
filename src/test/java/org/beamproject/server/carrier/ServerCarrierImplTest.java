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

import java.net.URL;
import org.beamproject.common.Server;
import org.beamproject.common.carrier.ServerCarrierModel;
import org.beamproject.server.ExecutorFake;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Before;
import org.junit.Test;

public class ServerCarrierImplTest {

    private final Server SERVER = Server.generate();
    private final byte[] MESSAGE = "myMessage".getBytes();
    private final String URL = "http://localhost";
    private final String PATH = "/myPath";
    private ExecutorFake executorFake;
    private HttpConnectionPool connectionPool;
    private HttpConnection connection;
    private HttpServer httpServer;
    private ServerCarrierModel model;
    private ServerCarrierImpl carrier;

    @Before
    public void setUp() {
        executorFake = new ExecutorFake();
        connectionPool = createMock(HttpConnectionPool.class);
        connection = createMock(HttpConnection.class);
        httpServer = createMock(HttpServer.class);
        model = createMock(ServerCarrierModel.class);
        carrier = new ServerCarrierImpl(model, executorFake, connectionPool, httpServer);
    }

    @Test
    public void testDeliverMessage() throws Exception {
        expect(connectionPool.borrowObject()).andReturn(connection);
        connection.post(new URL(URL), MESSAGE);
        expectLastCall();
        connectionPool.returnObject(connection);
        replay(connectionPool, connection);

        carrier.deliverMessage(MESSAGE, URL);

        verify(connectionPool, connection);
    }

    @Test
    public void testStartReceiving() {
        httpServer.setCarrier(carrier);
        expectLastCall();
        httpServer.start();
        expectLastCall();
        replay(httpServer);

        carrier.startReceiving();

        verify(httpServer);
    }

    @Test
    public void testStopReceiving() {
        httpServer.stop();
        expectLastCall();
        replay(httpServer);

        carrier.stopReceiving();

        verify(httpServer);
    }

    @Test
    public void testReceive() {
        model.consumeMessage(MESSAGE, PATH);
        expectLastCall();
        replay(model);

        carrier.receive(MESSAGE, PATH);

        verify(model);
    }

    @Test
    public void testShutdown() {
        connectionPool.close();
        expectLastCall();
        httpServer.stop();
        expectLastCall();
        replay(connectionPool, httpServer);

        carrier.shutdown();

        verify(connectionPool, httpServer);
    }

}
