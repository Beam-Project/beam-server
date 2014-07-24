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
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import org.beamproject.server.App;
import static org.beamproject.server.Event.*;
import static org.beamproject.server.util.Config.Key.*;
import org.beamproject.common.crypto.EncryptedConfig;
import org.beamproject.common.util.Files;
import org.beamproject.common.Server;
import org.beamproject.common.crypto.BouncyCastleIntegrator;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Executor;
import org.beamproject.common.carrier.CarrierException;
import org.beamproject.server.util.Config;

@Singleton
public class MainModel {

    private final Config.Key[] REQUIRED_CONFIG_KEYS = {PUBLIC_KEY, PRIVATE_KEY,
        SERVER_URL, SERVER_PORT, MQTT_HOST, MQTT_PORT,
        MQTT_USERNAME, MQTT_SUBSCRIBER_TOPIC};
    private final EventBus bus;
    private final Executor executor;
    private final Config config;
    private final Files files;
    @Getter
    private final Queue<String> execptions;
    @Getter
    private final Queue<Config.Key> missingConfigKeys;
    @Inject
    ClientCarrierModel clientCarrierModel;
    @Inject
    ServerCarrierModel serverCarrierModel;
    @Getter
    @Setter
    private Server server;

    @Inject
    public MainModel(EventBus bus, Executor executor, Config config, Files files) {
        this.bus = bus;
        this.executor = executor;
        this.config = config;
        this.files = files;
        this.execptions = new ConcurrentLinkedQueue<>();
        this.missingConfigKeys = new ConcurrentLinkedQueue<>();
    }

    public void bootstrap() {
        BouncyCastleIntegrator.initBouncyCastleProvider();

        if (isConfigSufficient()) {
            restoreServer();
            startCarriers();
        } else {
            bus.post(MISSING_CONFIG_ENTRIES);
        }
    }

    private boolean isConfigSufficient() {
        boolean isConfigSufficient = true;

        for (Config.Key key : REQUIRED_CONFIG_KEYS) {
            if (!config.contains(key)) {
                isConfigSufficient = false;
                missingConfigKeys.add(key);
            }
        }

        return isConfigSufficient;
    }

    private void restoreServer() {
        try {
            server = new Server(restoreMqttAddress(), restoreHttpUrl(), restoreKeyPair());
        } catch (MalformedURLException | IllegalArgumentException ex) {
            bus.post(INVALID_CONFIG_SERVER_URL);
        }
    }

    private URL restoreHttpUrl() throws MalformedURLException {
        return new URL(config.get(SERVER_URL));
    }

    private KeyPair restoreKeyPair() {
        byte[] publicKeyBytes = Base58.decode(config.get(PUBLIC_KEY));
        byte[] privateKeyBytes = Base58.decode(config.get(PRIVATE_KEY));
        return EccKeyPairGenerator.fromBothKeys(publicKeyBytes, privateKeyBytes);
    }

    private InetSocketAddress restoreMqttAddress() {
        String host = config.get(MQTT_HOST);
        int port = Integer.parseInt(config.get(MQTT_PORT));
        return new InetSocketAddress(host, port);
    }

    private void startCarriers() {
        boolean noExceptionOccurred = true;

        try {
            clientCarrierModel.startReceiving();
        } catch (CarrierException | IllegalArgumentException | IllegalStateException ex) {
            execptions.add("MQTT Carrier: " + ex.getMessage());
            bus.post(CARRIER_EXCEPTION);
            noExceptionOccurred = false;
        }

        try {
            serverCarrierModel.startReceiving();
        } catch (CarrierException | IllegalArgumentException | IllegalStateException ex) {
            execptions.add("HTTP Carrier: " + ex.getMessage());
            bus.post(CARRIER_EXCEPTION);
            noExceptionOccurred = false;
        }

        if (noExceptionOccurred) {
            bus.post(CARRIERS_STARTED);
        }
    }

    /**
     * Stores the {@link Config} and the {@link EncryptedConfig} via
     * {@link Properties} to the configured file paths.
     *
     * @throws IllegalStateException If no instance of {@link EncryptedConfig}
     * is available.
     */
    public void storeConfig() {
        files.storeProperies(config.getProperties(), App.CONFIG_PATH);
    }

    public void generateKeyPair(String filename) {
        Server newServer = Server.generate();
        Properties newConfig = new Properties();

        newConfig.setProperty(PUBLIC_KEY.toString(), newServer.getPublicKeyAsBase58());
        newConfig.setProperty(PRIVATE_KEY.toString(), newServer.getPrivateKeyAsBase58());

        try {
            files.storeProperies(newConfig, getCanonicalFilePath(filename));
            bus.post(KEY_PAIR_STORED);
        } catch (IllegalArgumentException | IOException ex) {
            execptions.add("Could not write the key pair to file: " + ex.getMessage());
            bus.post(COMMAND_LINE_EXCEPTION);
        }
    }

    private String getCanonicalFilePath(String filename) throws IOException {
        if (filename.substring(0, 1).contains("~")) {
            filename = filename.replaceFirst("~", System.getProperty("user.home"));
        }

        return new File(filename).getAbsolutePath();
    }

    public void shutdown() {
        storeConfig();

        try {
            clientCarrierModel.shutdown();
            serverCarrierModel.shutdown();
        } catch (IllegalStateException ex) {
        }

        executor.shutdown();
    }

}
