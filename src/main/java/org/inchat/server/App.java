/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-server.
 *
 * inchat-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.server;

import org.inchat.common.Config;
import org.inchat.common.Participant;
import org.inchat.common.util.Exceptions;

/**
 * This class provides static access to global instances, such as the
 * {@link Participant} or the {@link Config} classes.<p>
 * This is not a singleton since it is much easier to test the class this way.
 */
public class App {

    static Config config;

    /**
     * Sets a {@link Config} instance to this class.
     *
     * @param config The config to set. This may not be null.
     * @throws IllegalArgumentException If the argument is null.
     */
    public static void setConfig(Config config) {
        Exceptions.verifyArgumentNotNull(config);

        App.config = config;
    }

    public static Config getConfig() {
        return App.config;
    }
    
    public static Participant getParticipant() {
        return null;
    }

}
