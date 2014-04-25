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

import org.beamproject.common.Session;
import java.security.KeyPair;
import java.util.concurrent.ConcurrentHashMap;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.common.crypto.KeyPairCryptor;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;
import static org.beamproject.server.App.getConfig;
import static org.beamproject.server.App.storeConfig;
import org.beamproject.server.util.ComparableBytes;

public class Model {

    Participant server;
    ConcurrentHashMap<Participant, HandshakeResponder> activeHandshakes = new ConcurrentHashMap<>();
    ConcurrentHashMap<ComparableBytes, Session> activeSessions = new ConcurrentHashMap<>();

    /**
     * Gets the server, if loaded or loads it, if existing or creates and stores
     * a new one, if no server is existing.
     *
     * @return The server.
     */
    public Participant getServer() {
        if (server == null) {
            if (isEncryptedKeyPairStored()) {
                decryptServerFromConfig();
            } else {
                server = Participant.generate();
                EncryptedKeyPair encryptedKeyPair = KeyPairCryptor.encrypt(getConfig().keyPairPassword(), server.getKeyPair());
                getConfig().setProperty("keyPairSalt", encryptedKeyPair.getSalt());
                getConfig().setProperty("encryptedPublicKey", encryptedKeyPair.getEncryptedPublicKey());
                getConfig().setProperty("encryptedPrivateKey", encryptedKeyPair.getEncryptedPrivateKey());
                storeConfig();
            }
        }

        return server;
    }

    boolean isEncryptedKeyPairStored() {
        return getConfig().encryptedPublicKey() != null
                && getConfig().encryptedPrivateKey() != null;
    }

    void decryptServerFromConfig() {
        byte[] publicKey = Base58.decode(getConfig().encryptedPublicKey());
        byte[] privateKey = Base58.decode(getConfig().encryptedPrivateKey());
        byte[] salt = Base58.decode(getConfig().keyPairSalt());

        EncryptedKeyPair encryptedKeyPair = new EncryptedKeyPair(publicKey, privateKey, salt);
        KeyPair keyPair = KeyPairCryptor.decrypt(getConfig().keyPairPassword(), encryptedKeyPair);
        server = new Participant(keyPair);
    }

    /**
     * Gets a usable {@link HandshakeResponder} for the given user.
     * <p>
     * If the user has just initialized the authentication procedure, a new
     * {@link HandshakeResponder} is created, stored and returned.
     * <p>
     * If the authentication procedure was active before, the existing instance
     * is returned.
     *
     * @param user The user, who is involved in this handshake.
     * @return A valid {@link HandshakeResponder}.
     * @throws IllegalArgumentException If the argument is null.
     */
    public HandshakeResponder getHandshakeResponseByUser(Participant user) {
        Exceptions.verifyArgumentsNotNull(user);

        if (activeHandshakes.containsKey(user)) {
            return activeHandshakes.get(user);
        } else {
            activeHandshakes.put(user, new HandshakeResponder(server));
            return activeHandshakes.get(user);
        }
    }

    /**
     * Removes a active {@link HandshakeResponder} of the given user, if there is
     * one.
     * <p>
     * If the authentication procedure was active before, the existing instance
     * is returned.
     *
     * @param user The user, who is involved in this handshake.
     * @throws IllegalArgumentException If the argument is null.
     */
    public void destroyHandshakeResponseByUser(Participant user) {
        Exceptions.verifyArgumentsNotNull(user);

        activeHandshakes.remove(user);
    }

    /**
     * Adds a {@link Session} to this {@link Model}. If already as
     * {@link Session} with the given key exists, it will be overwritten.
     *
     * @param user The user who holds the session.
     * @param sessionKey The key of the session.
     * @throws IllegalArgumentException If at least one argument is null.
     */
    public void addSession(Participant user, byte[] sessionKey) {
        Exceptions.verifyArgumentsNotNull(user, sessionKey);

        activeSessions.put(new ComparableBytes(sessionKey), new Session(user, sessionKey));
    }

    /**
     * Gets the {@link Session} for the given session key.
     *
     * @param sessionKey The key of the session.
     * @return The associated {@link Session}.
     * @throws IllegalArgumentException If the argument is null.
     * @throws IllegalStateException If no session can be found the given key.
     */
    public Session getSessionByKey(byte[] sessionKey) {
        Exceptions.verifyArgumentsNotNull(sessionKey);

        if (!activeSessions.containsKey(new ComparableBytes(sessionKey))) {
            throw new IllegalStateException("The given key was not found. The existence should be checked first.");
        }

        return activeSessions.get(new ComparableBytes(sessionKey));
    }

    /**
     * Tells if a {@link Session}, represented by the given key, is stored in
     * this {@link Model}.
     *
     * @param sessionKey The session to look for.
     * @return true, when a {@link Session} can be found with the given key,
     * false otherwise.
     * @throws IllegalArgumentException If the argument is null.
     */
    public boolean isSessionExistingByKey(byte[] sessionKey) {
        Exceptions.verifyArgumentsNotNull(sessionKey);

        return activeSessions.containsKey(new ComparableBytes(sessionKey));
    }
}
