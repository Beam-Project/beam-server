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
package org.beamproject.server;

import static org.aeonbits.owner.Config.Sources;
import org.beamproject.common.util.ConfigBase;

@Sources({"file:~/.beam/server.conf"})
public interface Config extends ConfigBase {

    public final static String FOLDER = System.getProperty("user.home") + "/.beam/";
    public final static String FILE = FOLDER + "server.conf";

    @DefaultValue("keypair-password")
    String keyPairPassword();

    @DefaultValue("keypair-salt")
    String keyPairSalt();

    String encryptedPublicKey();

    String encryptedPrivateKey();

    String serverName();

    String serverUrl();
}