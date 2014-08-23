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
package org.beamproject.server.view;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.beamproject.server.App;
import org.beamproject.server.Event;
import org.beamproject.server.model.MainModel;
import org.beamproject.server.util.Config;
import static org.beamproject.server.util.Config.Key.MQTT_HOST;
import static org.beamproject.server.util.Config.Key.MQTT_PORT;
import static org.beamproject.server.util.Config.Key.MQTT_SUBSCRIBER_TOPIC;
import static org.beamproject.server.util.Config.Key.PRIVATE_KEY;
import static org.beamproject.server.util.Config.Key.PUBLIC_KEY;
import static org.beamproject.server.util.Config.Key.SERVER_URL;

public class CommandLineView {

    private final static String[] OPTION_g = {"g", "generate-key-pair",
        "generate a new key pair and stored it in FILE", "FILE"};
    private final static String[] OPTION_h = {"h", "help", "show this help"};

    private final MainModel model;
    private final EventBus bus;
    private final Config config;
    private Options options;
    private CommandLineParser parser;
    private CommandLine commandLine;

    @Inject
    public CommandLineView(MainModel model, EventBus bus, Config config) {
        this.model = model;
        this.bus = bus;
        this.config = config;
        this.bus.register(this);

        defineOptions();
        defineParser();
        captureSigint();
    }

    private void defineOptions() {
        options = new Options();

        options.addOption(OptionBuilder
                .withLongOpt(OPTION_g[1])
                .withDescription(OPTION_g[2])
                .hasArg()
                .withArgName(OPTION_g[3])
                .create(OPTION_g[0]));
        options.addOption(OPTION_h[0], OPTION_h[1], false, OPTION_h[2]);
    }

    private void defineParser() {
        parser = new BasicParser();
    }

    private void captureSigint() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                model.shutdown();
            }
        });
    }

    public void parse(String[] args) {
        showNameAndVersion();

        try {
            commandLine = parser.parse(options, args);
            batchCommandLineOptions();
        } catch (ParseException ex) {
            printCommandLineMisusage();
        }
    }

    private void showNameAndVersion() {
        System.out.println(App.NAME + " " + App.POM_VERSION + " - " + App.WEBSITE);
    }

    private void batchCommandLineOptions() {
        boolean hasOptions = false;

        if (commandLine.hasOption(OPTION_g[0])) {
            hasOptions = true;
            String filename = commandLine.getOptionValue(OPTION_g[0]);
            model.generateKeyPair(filename);
        }

        if (commandLine.hasOption(OPTION_h[0])) {
            hasOptions = true;
            printHelp();
        }

        if (!hasOptions) {
            model.bootstrap();
        }
    }

    @Subscribe
    public void printMessage(Event event) {
        switch (event) {
            case MISSING_CONFIG_ENTRIES:
                printMissingConfig();
                break;
            case INVALID_CONFIG_SERVER_URL:
                invalidConfig(SERVER_URL);
                break;
            case INVALID_COMMAND_LINE_USAGE:
                printCommandLineMisusage();
                break;
            case COMMAND_LINE_EXCEPTION:
                printCommandLineExceptions();
                break;
            case KEY_PAIR_STORED:
                printKeyPairStoredMessage();
                break;
            case CARRIER_EXCEPTION:
                System.out.println(model.getExecptions().poll());
                break;
            case SERVER_CONFIGURATION_LOADED:
                printServerConfigurationLoaded();
                break;
            case CARRIERS_STARTED:
                System.out.println("MQTT Carrier connected to broker at "
                        + config.get(MQTT_HOST) + ":" + config.get(MQTT_PORT)
                        + ", subscribed to '" + config.get(MQTT_SUBSCRIBER_TOPIC) + "'.");
                System.out.println("HTTP Carrier listening on "
                        + config.get(SERVER_URL) + ".");
                break;
        }
    }

    private void printMissingConfig() {
        String list = "";

        while (!model.getMissingConfigKeys().isEmpty()) {
            list += ", " + model.getMissingConfigKeys().remove().toString();
        }

        list = list.replaceFirst(", ", "");

        System.out.println("Missing the configuration entry/entries: " + list + ".");
    }

    private void invalidConfig(Config.Key key) {
        System.out.println("Invalid configuration entry: " + key.toString());
    }

    private void printCommandLineMisusage() {
        System.out.println("This command could not be understood.");
        printHelp();
    }

    private void printCommandLineExceptions() {
        while (!model.getExecptions().isEmpty()) {
            System.out.println(model.getExecptions().remove());
        }
    }

    private void printKeyPairStoredMessage() {
        System.out.println("Key pair successfully generated and stored.");
        System.out.println(" - " + PUBLIC_KEY + ": The public key encoded as X509, represented as Base58 string.");
        System.out.println(" - " + PRIVATE_KEY + ": The private key encoded as PKCS8, represented as Base58 string.");
    }

    private void printServerConfigurationLoaded() {
        System.out.println("Server configruation is complete. Beam address is:");
        System.out.println(model.getServer().getAddress());
    }

    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar beam-server.jar", options);
    }

}
