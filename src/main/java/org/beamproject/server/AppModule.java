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
import com.google.inject.MembersInjector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import java.lang.reflect.Field;
import java.util.Properties;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.beamproject.common.carrier.ClientCarrier;
import org.beamproject.common.carrier.ClientCarrierImpl;
import org.beamproject.common.carrier.ClientCarrierModel;
import org.beamproject.common.carrier.MqttConnectionPoolFactory;
import org.beamproject.common.carrier.ServerCarrier;
import org.beamproject.common.carrier.ServerCarrierModel;
import org.beamproject.common.crypto.CryptoPackerPool;
import org.beamproject.common.crypto.CryptoPackerPoolFactory;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Files;
import org.beamproject.server.carrier.HttpConnectionPoolFactory;
import org.beamproject.server.carrier.HttpServer;
import org.beamproject.server.carrier.ServerCarrierImpl;
import org.beamproject.server.model.ClientCarrierModelImpl;
import org.beamproject.server.model.MainModel;
import org.beamproject.server.model.ServerCarrierModelImpl;
import org.beamproject.server.util.Config;
import static org.beamproject.server.util.Config.Key.MQTT_HOST;
import static org.beamproject.server.util.Config.Key.MQTT_PORT;
import static org.beamproject.server.util.Config.Key.MQTT_SUBSCRIBER_TOPIC;
import static org.beamproject.server.util.Config.Key.MQTT_USERNAME;
import org.beamproject.server.view.CommandLineView;

public class AppModule extends AbstractModule {
    
    private final static Logger log = getLogger(AppModule.class.getName());

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

        // Logger
        bindListener(Matchers.any(), new LogTypeListener());
    }

    @Provides
    @Singleton
    EventBus providesEventBus() {
        return new EventBus(new SubscriberExceptionHandler() {
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                log.log(WARNING, "EventBus exception occurred: {0}", context.getSubscriber().toString());
                log.log(WARNING, "Method: {0}", context.getSubscriberMethod().toString());
                log.log(WARNING, readStacktrace(exception));
            }
        });
    }

    private static String readStacktrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();

        for (StackTraceElement stacktrace1 : throwable.getStackTrace()) {
            builder.append(stacktrace1.toString());
        }

        return builder.toString();
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
        Config config = providesConfig();

        return new MqttConnectionPoolFactory(config.get(MQTT_HOST),
                Integer.parseInt(config.get(MQTT_PORT)),
                config.get(MQTT_USERNAME),
                config.get(MQTT_SUBSCRIBER_TOPIC));
    }

    @Provides
    @Singleton
    HttpConnectionPoolFactory providesHttpConnectionPoolFactory() {
        return new HttpConnectionPoolFactory();
    }

    @Provides
    @Singleton
    CryptoPackerPool providesCryptoPackerPool() {
        CryptoPackerPoolFactory factory = new CryptoPackerPoolFactory();
        return new CryptoPackerPool(factory);
    }

    private class LogTypeListener implements TypeListener {

        @Override
        public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
            for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
                if (field.getType() == Logger.class) {
                    typeEncounter.register(new LogMemberInjector<T>(field));
                }
            }
        }
    }

    private class LogMemberInjector<T> implements MembersInjector<T> {

        private final Field field;
        private final Logger logger;

        LogMemberInjector(Field field) {
            this.field = field;
            this.logger = getLogger(field.getDeclaringClass().getName());
            field.setAccessible(true);
        }

        @Override
        public void injectMembers(T t) {
            try {
                field.set(t, logger);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
