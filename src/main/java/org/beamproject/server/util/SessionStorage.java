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
import org.beamproject.common.Session;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.ComparableBytes;

/**
 * A {@link SessionStorage} stores {@link Session}s in a thread-safe way.
 *
 * @see Session
 */
public class SessionStorage {

    private static final long serialVersionUID = 1L;
    final ConcurrentHashMap<ComparableBytes, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Tells whether a {@link Session} for the the given session key is existing
     * or not.
     *
     * @param sessionKey The session key to look for.
     * @return true, when a {@link Session} could be found with, false
     * otherwise.
     */
    public boolean isSessionExisting(byte[] sessionKey) {
        if (sessionKey == null) {
            return false;
        }

        return sessions.containsKey(new ComparableBytes(sessionKey));
    }

    /**
     * Sets the given session key as {@link Session} in this storage.
     *
     * @param remoteParticipant The remote participant of this {@link Session}.
     * @param sessionKey The session key to set.
     */
    public void setSession(Participant remoteParticipant, byte[] sessionKey) {
        sessions.put(new ComparableBytes(sessionKey), new Session(remoteParticipant, sessionKey));
        System.out.println("added session with user: " + remoteParticipant.getPublicKeyAsBase58());
    }

    /**
     * Gets the {@link Session} of this session key.
     *
     * @param sessionKey The key of this session.
     * @return The {@link Session} object.
     * @throws IllegalStateException If there was no {@link Session} stored for
     * this key.
     */
    public Session getSessionByKey(byte[] sessionKey) {
        if (isSessionExisting(sessionKey)) {
            return sessions.get(new ComparableBytes(sessionKey));
        }

        throw new IllegalStateException("Could not find a session with the given session key.");
    }

    /**
     * Removes an active {@link Session} with the given key from the storage and
     * invalidates the key and participant of the {@link Session} object.
     *
     * @param sessionKey The key of the session to remove.
     */
    public void removeSession(byte[] sessionKey) {
        Session session = sessions.remove(new ComparableBytes(sessionKey));

        if (session != null) {
            session.invalidateSession();
        }
    }

}
