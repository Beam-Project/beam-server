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

import org.beamproject.common.carrier.ClientCarrierModel;
import org.beamproject.common.carrier.ServerCarrierModel;
import java.security.Security;
import java.util.Properties;
import org.beamproject.server.BusFake;
import static org.beamproject.server.Event.*;
import static org.beamproject.server.util.Config.Key.*;
import org.beamproject.common.util.Files;
import org.beamproject.common.Server;
import org.beamproject.common.crypto.BouncyCastleIntegrator;
import org.beamproject.server.App;
import org.beamproject.server.ExecutorFake;
import org.beamproject.common.carrier.CarrierException;
import org.beamproject.server.util.Config;
import static org.easymock.EasyMock.*;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class MainModelTest {

    private final Server SERVER = Server.generate();
    private final String PORT = "8080";
    private final String MQTT_HOST = "127.0.0.1";
    private final String MQTT_PORT = "3625";
    private final String MQTT_USERNAME = "Mr Spock";
    private final String MQTT_SUBSCRIBER_TOPIC = "in";
    private BusFake busFake;
    private ExecutorFake executorFake;
    private MainModel model;
    private Config config;
    private Files files;
    private ClientCarrierModel clientCarrierModel;
    private ServerCarrierModel serverCarrierModel;

    @Before
    public void setUp() {
        busFake = new BusFake();
        config = new Config(new Properties());
        files = createMock(Files.class);
        model = new MainModel(busFake.getBus(), executorFake, config, files);
        clientCarrierModel = createMock(ClientCarrierModel.class);
        serverCarrierModel = createMock(ServerCarrierModel.class);
        model.clientCarrierModel = clientCarrierModel;
        model.serverCarrierModel = serverCarrierModel;
    }

    @After
    public void verifyBus() {
        busFake.verify();
    }

    @Test
    public void testBootstrapOnBouncyCastleIntegration() {
        Security.removeProvider(BouncyCastleIntegrator.PROVIDER_NAME);

        model.bootstrap();

        assertTrue(Security.getProvider(BouncyCastleIntegrator.PROVIDER_NAME) != null);
        busFake = new BusFake(); // to verify empty
    }

    @Test
    public void testBootstrapOnMissingConfiguration() {
        model.bootstrap();

        assertEquals(MISSING_CONFIG_ENTRIES, busFake.getNextEvent());

        assertEquals(PUBLIC_KEY, model.getMissingConfigKeys().remove());
        assertEquals(PRIVATE_KEY, model.getMissingConfigKeys().remove());
        assertEquals(SERVER_URL, model.getMissingConfigKeys().remove());
        assertEquals(SERVER_PORT, model.getMissingConfigKeys().remove());
        assertEquals(MQTT_BROKER_HOST, model.getMissingConfigKeys().remove());
        assertEquals(MQTT_BROKER_PORT, model.getMissingConfigKeys().remove());
        assertEquals(MQTT_BROKER_USERNAME, model.getMissingConfigKeys().remove());
        assertEquals(MQTT_BROKER_SUBSCRIBER_TOPIC, model.getMissingConfigKeys().remove());
    }

    @Test
    public void testBootstrapOnInvalidUrl() {
        fillConfig();
        config.set(SERVER_URL, "invalid url");

        model.bootstrap();

        assertEquals(INVALID_CONFIG_SERVER_URL, busFake.getNextEvent());
        assertEquals(CARRIERS_STARTED, busFake.getNextEvent());
    }

    private void fillConfig() {
        config.set(PUBLIC_KEY, SERVER.getPublicKeyAsBase58());
        config.set(PRIVATE_KEY, SERVER.getPrivateKeyAsBase58());
        config.set(SERVER_URL, SERVER.getUrl().toString());
        config.set(SERVER_PORT, PORT);
        config.set(MQTT_BROKER_HOST, MQTT_HOST);
        config.set(MQTT_BROKER_PORT, MQTT_PORT);
        config.set(MQTT_BROKER_USERNAME, MQTT_USERNAME);
        config.set(MQTT_BROKER_SUBSCRIBER_TOPIC, MQTT_SUBSCRIBER_TOPIC);
    }

    @Test
    public void testBootstrap() {
        fillConfig();
        clientCarrierModel.startReceiving();
        expectLastCall();
        serverCarrierModel.startReceiving();
        expectLastCall();
        replay(clientCarrierModel, serverCarrierModel);

        model.bootstrap();

        verify(clientCarrierModel, serverCarrierModel);
        assertEquals(SERVER, model.getServer());
        assertEquals(CARRIERS_STARTED, busFake.getNextEvent());
    }

    @Test
    public void testBootstrapOnClientCarrierException() {
        fillConfig();
        clientCarrierModel.startReceiving();
        expectLastCall().andThrow(new CarrierException("myException"));
        serverCarrierModel.startReceiving();
        expectLastCall();
        replay(clientCarrierModel, serverCarrierModel);

        model.bootstrap();

        verify(clientCarrierModel, serverCarrierModel);
        assertEquals(CARRIER_EXCEPTION, busFake.getNextEvent());
        assertTrue(model.getExecptions().poll().contains("myException"));
    }

    @Test
    public void testBootstrapOnServerCarrierException() {
        fillConfig();
        clientCarrierModel.startReceiving();
        expectLastCall();
        serverCarrierModel.startReceiving();
        expectLastCall().andThrow(new CarrierException("myException"));
        replay(clientCarrierModel, serverCarrierModel);

        model.bootstrap();

        verify(clientCarrierModel, serverCarrierModel);
        assertEquals(CARRIER_EXCEPTION, busFake.getNextEvent());
        assertTrue(model.getExecptions().poll().contains("myException"));
    }

    @Test
    public void testStoreConfig() {
        files.storeProperies(config.getProperties(), App.CONFIG_PATH);
        expectLastCall();
        replay(files);

        model.storeConfig();

        verify(files);
    }

    @Test
    public void testGenerateKeyPairOnIllegalPath() {
        files.storeProperies(anyObject(Properties.class), anyString());
        expectLastCall().andThrow(new IllegalArgumentException());
        replay(files);

        model.generateKeyPair("\\//\\//\\//////\\\\  ");

        assertEquals(INVALID_COMMAND_LINE_USAGE, busFake.getNextEvent());
        verify(files);
    }

    @Test
    public void testGenerateKeyPair() {
        files.storeProperies(anyObject(Properties.class), anyString());
        expectLastCall();
        replay(files);

        model.generateKeyPair("test.conf");

        verify(files);
    }

}
