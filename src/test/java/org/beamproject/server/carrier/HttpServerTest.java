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

import org.beamproject.common.carrier.ServerCarrier;
import org.beamproject.server.ExecutorFake;
import org.beamproject.server.util.Config;
import static org.easymock.EasyMock.createMock;
import org.junit.Test;
import org.junit.Before;

public class HttpServerTest {

    private ServerCarrier carrier;
    private ExecutorFake executorFake;
    private Config config;
    private HttpServer server;

    @Before
    public void setUp() {
        carrier = createMock(ServerCarrier.class);

        executorFake = new ExecutorFake();
        server = new HttpServer(executorFake, config);
    }

    @Test(expected = IllegalStateException.class)
    public void testStartOnMissingCarrier() {
        server.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStartOnMissingPort() {
        server.setCarrier(carrier);
        server.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopOnMissingConnection() {
        server.stop();
    }

}
