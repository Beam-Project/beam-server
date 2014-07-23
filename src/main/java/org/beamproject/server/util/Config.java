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
package org.beamproject.server.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;
import lombok.Getter;
import org.beamproject.common.Server;

/**
 * This class provides a simple wrapper around {@link Properties} for the ease
 * of use.
 */
public class Config {

    /**
     * Defines what keys are available for the internally used
     * {@link Properties}.
     */
    public enum Key {

        /**
         * The {@link PublicKey} of the {@link Server}. The bytes of the public
         * key, encoded as X509, are Base58 encoded to a String.
         */
        PUBLIC_KEY,
        /**
         * The {@link PrivateKey} of the {@link Server}. The bytes of the
         * private key, encoded as PKCS8, are Base58 encoded to a String.
         */
        PRIVATE_KEY,
        /**
         * The URL of the {@link Server}. This is part of its Beam address.
         */
        SERVER_URL,
        /**
         * The port at which the {@link Server} listens for incoming HTTP
         * connections.
         */
        SERVER_PORT,
        /**
         * The host name of the MQTT broker used to connect to clients.
         */
        MQTT_HOST,
        /**
         * The port at which the MQTT broker listens.
         */
        MQTT_PORT,
        /**
         * The username of this beam-server for connecting to the MQTT broker.
         */
        MQTT_USERNAME,
        /**
         * The topic to which this beam-server has to subscribe to receive
         * incoming messages from its clients.
         */
        MQTT_SUBSCRIBER_TOPIC,
    }
    @Getter
    private final Properties properties;

    /**
     * Uses the given {@link Properties} as store of key/value pairs.
     *
     * @param properties The properties to use.
     */
    public Config(Properties properties) {
        this.properties = properties;
    }

    /**
     * Sets the given {@link Key} in combination with the given {@code value}.
     *
     * @param key The key for the value.
     * @param value The value itself.
     */
    public void set(Key key, String value) {
        properties.setProperty(key.toString(), value);
    }

    /**
     * Gets the value, stored under the given {@link Key}.
     *
     * @param key The key to look for.
     * @return The value of the key/value pair or {@code null}, if the key
     * cannot be found in this {@link Config}.
     */
    public String get(Key key) {
        return properties.getProperty(key.toString());
    }

    /**
     * Tells whether the given {@link Key} is stored in this {@link Config}.
     *
     * @param key A key to look for.
     * @return true, if a key/value pair with the given key exists in this
     * {@link Config}, false otherwise.
     */
    public boolean contains(Key key) {
        return properties.containsKey(key.toString());
    }

}
