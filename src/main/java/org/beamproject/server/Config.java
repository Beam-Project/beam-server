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

import static org.aeonbits.owner.Config.Sources;
import org.beamproject.common.util.ConfigBase;

/**
 * This class is used for configuration purposes and interacts with the OWNER
 * library (see: http://owner.aeonbits.org/).
 *
 * @see ConfigBase
 */
@Sources({"file:~/.beam/server.conf${developmentExtension}"})
public interface Config extends ConfigBase {

    public final static String FOLDER = System.getProperty("user.home") + "/.beam/";
    public final static String FILE = "server.conf";

    @DefaultValue("keypair-password")
    String keyPairPassword();

    @DefaultValue("keypair-salt")
    String keyPairSalt();

    String encryptedPublicKey();

    String encryptedPrivateKey();

    String serverName();

    @DefaultValue("http://localhost:8080/beam-server/in/")
    String serverUrl();

    @DefaultValue("20000")
    long maxHeartbeatTimestampDeviationInMilliseconds();
}
