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
package org.beamproject.server;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Properties;
import org.beamproject.server.model.MainModel;
import org.beamproject.common.util.Files;
import org.beamproject.common.util.Executor;
import org.beamproject.server.carrier.HttpConnectionPoolFactory;
import org.beamproject.common.carrier.ClientCarrier;
import org.beamproject.server.carrier.ClientCarrierImpl;
import org.beamproject.server.carrier.HttpServer;
import org.beamproject.server.carrier.MqttConnectionPoolFactory;
import org.beamproject.common.carrier.ServerCarrier;
import org.beamproject.server.carrier.ServerCarrierImpl;
import org.beamproject.common.carrier.ClientCarrierModel;
import org.beamproject.server.model.ClientCarrierModelImpl;
import org.beamproject.common.carrier.ServerCarrierModel;
import org.beamproject.server.model.ServerCarrierModelImpl;
import org.beamproject.server.util.Config;
import org.beamproject.server.view.CommandLineView;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        // Models
        bind(MainModel.class);
        bind(ClientCarrierModel.class).to(ClientCarrierModelImpl.class);
        bind(ServerCarrierModel.class).to(ServerCarrierModelImpl.class);
        bind(ClientCarrier.class).to(ClientCarrierImpl.class);
        bind(ServerCarrier.class).to(ServerCarrierImpl.class);
        bind(HttpServer.class);

        // Views
        bind(CommandLineView.class);

        // Utils
        bind(Files.class);
    }

    @Provides
    @Singleton
    EventBus providesEventBus() {
        return new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                System.out.println("EventBus exception occurred: " + context.getSubscriber().toString());
                System.out.println("Method: " + context.getSubscriberMethod().toString());
                System.out.println("");
                exception.printStackTrace();
            }
        });
    }

    @Provides
    @Singleton
    Config providesConfig() {
        Files files = new Files();
        Properties properties = files.loadConfigIfAvailable(App.CONFIG_PATH);
        return new Config(properties);
    }

    @Provides
    @Singleton
    Executor providesExecutor() {
        return new Executor();
    }

    @Provides
    @Singleton
    MqttConnectionPoolFactory providesMqttConnectionPoolFactory() {
        return new MqttConnectionPoolFactory(providesConfig());
    }

    @Provides
    @Singleton
    HttpConnectionPoolFactory providesHttpConnectionPoolFactory() {
        return new HttpConnectionPoolFactory();
    }

}
