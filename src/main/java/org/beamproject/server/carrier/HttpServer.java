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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import lombok.Setter;
import org.beamproject.common.carrier.CarrierException;
import org.beamproject.common.carrier.ServerCarrier;
import org.beamproject.common.util.Base64;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;
import org.beamproject.server.util.Config;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * A HTTP server using the <a href="http://www.simpleframework.org/">Simple
 * Framework</a>.
 * <p>
 * This is used by {@link ServerCarrier} to send and receive messages via HTTP.
 *
 * @see ServerCarrier
 */
public class HttpServer implements Container {

    public final static String POST_MESSAGE_KEY = "MSG";
    private final static String HEADER_CONTENT_TYPE_KEY = "Content-Type";
    private final static String HEADER_CONTENT_TYPE_VALUE = "text/plain";
    private final static String HEADER_SERVER_KEY = "Server";
    private final static String HEADER_SERVER_VALUE = "Server/1.0";
    private final static String HEADER_DATE_KEY = "Date";
    private final static String HEADER_LAST_MODIFIED_KEY = "Last-Modified";
    private final Executor executor;
    private final Config config;
    private Server server;
    private SocketAddress address;
    private Connection connection;
    @Setter
    private ServerCarrier carrier;

    @Inject
    public HttpServer(Executor executor, Config config) {
        this.executor = executor;
        this.config = config;
    }

    /**
     * Starts the server, therefor makes it listening a the configured port (in
     * the {@link Config} instance, referenced by the key
     * {@link Config.Key.SERVER_PORT}).
     *
     * @throws IllegalStateException If the {@link ServerCarrier} has not been
     * set.
     * @throws CarrierException If the server could not be started.
     */
    public void start() {
        if (carrier == null) {
            throw new IllegalStateException("The ServerCarrier has to be set.");
        }

        try {
            server = new ContainerServer(this);
            connection = new SocketConnection(server);
            address = new InetSocketAddress(getPort());

            connection.connect(address);
        } catch (IOException ex) {
            throw new CarrierException("Could not create HTTP server: " + ex.getMessage());
        }
    }

    private int getPort() {
        try {
            URL url = new URL(config.get(Config.Key.SERVER_URL));
            int candidate = url.getPort();

            return candidate == -1
                    ? url.getDefaultPort()
                    : candidate;
        } catch (MalformedURLException | NullPointerException ex) {
            throw new IllegalStateException("The server address is not valid: " + ex.getMessage());
        }
    }

    /**
     * Stops the server.
     *
     * @throws IllegalStateException If the server was not running before.
     * @throws CarrierException If the server could not be stopped.
     */
    public void stop() {
        if (connection == null) {
            throw new IllegalStateException("The HTTP server is not connected.");
        }

        try {
            connection.close();
        } catch (IOException ex) {
            throw new CarrierException("Could not disconnect HTTP server: " + ex.getMessage());
        }
    }

    @Override
    public void handle(final Request request, final Response response) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                try {
                    long date = System.currentTimeMillis();

                    response.setValue(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE);
                    response.setValue(HEADER_SERVER_KEY, HEADER_SERVER_VALUE);
                    response.setDate(HEADER_DATE_KEY, date);
                    response.setDate(HEADER_LAST_MODIFIED_KEY, date);
                    response.setStatus(Status.NO_CONTENT);

                    carrier.receive(readPostData(request));
                } catch (Exception ex) {
                    throw new CarrierException("Could not handle HTTP request: " + ex.getMessage());
                } finally {
                    try {
                        response.close();
                    } catch (IOException ex) {
                        throw new CarrierException("Could not close the HTTP request: " + ex.getMessage());
                    }
                }
            }
        });
    }

    private byte[] readPostData(Request request) {
        Query query = request.getQuery();
        return Base64.decode(query.get(POST_MESSAGE_KEY));
    }

}
