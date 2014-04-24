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

import java.sql.Timestamp;
import java.util.Date;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.HandshakeChallenge;
import org.beamproject.common.crypto.HandshakeResponse;
import org.beamproject.common.util.Exceptions;

/**
 * Stores a currently valid session between this server and an other
 * {@link Participant}.
 *
 * @see Handshake
 * @see HandshakeChallenge
 * @see HandshakeResponse
 */
public class Session {

    Participant user;
    byte[] key;
    Timestamp creationTimestamp;

    /**
     * Creates a new instance of {@link Session}, initialized with the given
     * user and the already agreed session key.
     * <p>
     * To create the session key, the user and this server have to complete a
     * {@link Handshake}.
     *
     * @param user The user who holds this session key.
     * @param key The key of this session.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public Session(Participant user, byte[] key) {
        Exceptions.verifyArgumentsNotNull(user, key);

        this.user = user;
        this.key = key;
        creationTimestamp = new Timestamp(new Date().getTime());
    }

    /**
     * @return The user who holds this session.
     */
    public Participant getUser() {
        return user;
    }

    /**
     * @return The key of this session.
     */
    public byte[] getKey() {
        return key;
    }
}
