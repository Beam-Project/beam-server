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

public class ConfigTest {

    /**
     * Loads the {@link Config} and puts it into the {@link App} for global
     * access. It is configured that it never reads from possibly existing local
     * config files.
     * <p>
     * This does not prevent form any possible writings!
     */
    public static void loadDefaultConfig() {
        ConfigFactory.setProperty("developmentExtension", "INVALID-PATH");
        AppTest.setAppConfig(ConfigFactory.create(Config.class));
    }

}