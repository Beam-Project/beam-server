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
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.beamproject.server.util.Config;
import static org.beamproject.server.util.Config.Key.*;
import org.fusesource.mqtt.client.MQTT;

/**
 * This factory class is required by the Apache Commons Pool library. It
 * provides methods to create {@link MqttConnection} objects.
 */
public class MqttConnectionPoolFactory extends BasePooledObjectFactory<MqttConnection> {

    private final Config config;

    @Inject
    public MqttConnectionPoolFactory(Config config) {
        this.config = config;
    }

    @Override
    public MqttConnection create() throws Exception {
        MqttConnection connection = new MqttConnection(new MQTT(),
                config.get(MQTT_BROKER_HOST),
                getPort(),
                config.get(MQTT_BROKER_USERNAME),
                config.get(MQTT_BROKER_SUBSCRIBER_TOPIC));

        connection.connect();

        return connection;
    }

    private int getPort() {
        try {
            return Integer.parseInt(config.get(MQTT_BROKER_PORT));
        } catch (NumberFormatException ex) {
            throw new CarrierException("The port number is invalid: " + ex.getMessage());
        }
    }

    @Override
    public void destroyObject(PooledObject<MqttConnection> pooledObject) throws Exception {
        pooledObject.getObject().disconnect();
    }

    @Override
    public PooledObject<MqttConnection> wrap(MqttConnection obj) {
        return new DefaultPooledObject<>(obj);
    }

}
