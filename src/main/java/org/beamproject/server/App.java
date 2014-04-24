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

import org.aeonbits.owner.ConfigFactory;
import org.beamproject.common.util.ConfigWriter;

/**
 * This class initializes the server and holds {@link Config} and {@link Model}.
 */
public class App {

    protected static ConfigWriter configWriter;
    protected static Config config;
    protected static Controller controller;
    protected static Model model;

    static {
        loadConfig();
        loadControllerAndModel();
    }

    private static void loadConfig() {
        configWriter = new ConfigWriter();
        config = ConfigFactory.create(Config.class);
    }

    private static void loadControllerAndModel() {
        controller = new Controller();
        model = new Model();
    }

    public static Config getConfig() {
        return config;
    }

    public static Controller getController() {
        return controller;
    }

    public static Model getModel() {
        return model;
    }

    public static void storeConfig() {
        configWriter.writeConfig(config, Config.FOLDER, Config.FILE);
    }

}
