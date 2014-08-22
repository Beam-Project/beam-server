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
package org.beamproject.server.util;

import java.util.concurrent.ConcurrentHashMap;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.Handshake;
import org.beamproject.common.crypto.HandshakeChallenger;
import org.beamproject.common.crypto.HandshakeResponder;

/**
 * A {@link HandshakeStorage} may store {@link HandshakeResponder}s or
 * {@link HandshakeChallenger}s in a thread-safe way (using
 * {@link ConcurrentHashMap}s).
 *
 * @param <T> The subtype of {@link Handshake} that may be stored.
 * @see Handshake
 * @see HandshakeChallenger
 * @see HandshakeResponder
 */
public class HandshakeStorage<T extends Handshake> {

    private static final long serialVersionUID = 1L;
    final ConcurrentHashMap<Participant, T> handshakes = new ConcurrentHashMap<>();

    /**
     * Tells whether a handshake for the the given participant is existing or
     * not.
     *
     * @param remoteParticipant The participant to look for.
     * @return true, when a {@link Handshake} could be found with, false
     * otherwise.
     */
    public boolean isHandshakeExisting(Participant remoteParticipant) {
        if (remoteParticipant == null) {
            return false;
        }

        return handshakes.containsKey(remoteParticipant);
    }

    /**
     * Sets the given handshake in this storage, identified via remote
     * participant.
     *
     * @param remoteParticipant The remote participant (key of the pair).
     * @param handshake The handshake to set (value of the pair).
     */
    public void setHandshake(Participant remoteParticipant, T handshake) {
        handshakes.put(remoteParticipant, handshake);
    }

    /**
     * Gets a usable {@link HandshakeResponder} for the given participant.
     *
     * @param remoteParticipant The remote participant, who is involved in this
     * handshake.
     * @return The handshake.
     * @throws IllegalStateException If there was no handshake stored for this
     * participant.
     */
    public T getHandshake(Participant remoteParticipant) {
        if (isHandshakeExisting(remoteParticipant)) {
            return handshakes.get(remoteParticipant);
        }

        throw new IllegalStateException("Could not find a handshake for the given participant.");
    }

    /**
     * Removes an active {@link Handshake} of the given participant, if there is
     * one.
     *
     * @param remoteParticipant The participant, involved in this handshake.
     */
    public void removeHandshake(Participant remoteParticipant) {
        handshakes.remove(remoteParticipant);
    }

}
