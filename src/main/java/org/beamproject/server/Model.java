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

import java.security.KeyPair;
import java.util.concurrent.ConcurrentHashMap;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.HandshakeResponse;
import org.beamproject.common.crypto.KeyPairCryptor;
import org.beamproject.common.util.Base58;
import org.beamproject.common.util.Exceptions;
import static org.beamproject.server.App.getConfig;
import static org.beamproject.server.App.storeConfig;

public class Model {

    Participant server;
    ConcurrentHashMap<Participant, HandshakeResponse> activeHandshakes = new ConcurrentHashMap<>();

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

    public HandshakeResponse getHandshakeResponseByUser(Participant user) {
        Exceptions.verifyArgumentsNotNull(user);

        if (activeHandshakes.contains(user)) {
            return activeHandshakes.get(user);
        } else {
            activeHandshakes.put(user, new HandshakeResponse(server));
            return activeHandshakes.get(user);
        }
    }
}
