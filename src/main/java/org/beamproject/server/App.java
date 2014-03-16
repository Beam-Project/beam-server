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
import org.beamproject.common.Config;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.KeyPairCryptor;

/**
 * This class provides static access to global instances, such as the
 * {@link Participant} or the {@link Config} classes.<p>
 * This is not a singleton since it is much easier to test the class this way.
 */
public class App {

    public final static String DEFAULT_KEY_PAIR_PASSWORD = "default-password";
    static String CONFIG_FILENAME = "beam-server.conf";
    static Config config;
    static Participant participant;

    /**
     * Load environment when first accessing this class.
     */
    static {
        loadConfig();
        loadParticipant();
    }

    private static void loadConfig() {
        config = new Config(CONFIG_FILENAME);
    }

    private static void loadParticipant() {
        if (isEncryptedKeyPairStored()) {
            readAndDecryptParticipantFromConfig();
        } else {
            generateAndStoreParticipant();
        }
    }

    private static boolean isEncryptedKeyPairStored() {
        return config.isKeyExisting(ServerConfigKey.keyPairPassword)
                && config.isKeyExisting(ServerConfigKey.keyPairSalt)
                && config.isKeyExisting(ServerConfigKey.encryptedPublicKey)
                && config.isKeyExisting(ServerConfigKey.encryptedPrivateKey);
    }

    private static void readAndDecryptParticipantFromConfig() {
        String password = config.getProperty(ServerConfigKey.keyPairPassword);
        String salt = config.getProperty(ServerConfigKey.keyPairSalt);
        String encryptedPublicKey = config.getProperty(ServerConfigKey.encryptedPublicKey);
        String encryptedPrivateKey = config.getProperty(ServerConfigKey.encryptedPrivateKey);
        EncryptedKeyPair encryptedKeyPair = new EncryptedKeyPair(encryptedPublicKey, encryptedPrivateKey, salt);

        KeyPair keyPair = KeyPairCryptor.decrypt(password, encryptedKeyPair);
        participant = new Participant(keyPair);
    }

    private static void generateAndStoreParticipant() {
        participant = new Participant(EccKeyPairGenerator.generate());

        EncryptedKeyPair encryptedKeyPair = KeyPairCryptor.encrypt(DEFAULT_KEY_PAIR_PASSWORD, participant.getKeyPair());
        App.config.setProperty(ServerConfigKey.keyPairPassword, DEFAULT_KEY_PAIR_PASSWORD);
        App.config.setProperty(ServerConfigKey.keyPairSalt, encryptedKeyPair.getSalt());
        App.config.setProperty(ServerConfigKey.encryptedPublicKey, encryptedKeyPair.getEncryptedPublicKey());
        App.config.setProperty(ServerConfigKey.encryptedPrivateKey, encryptedKeyPair.getEncryptedPrivateKey());
    }

    public static Config getConfig() {
        return App.config;
    }

    public static Participant getParticipant() {
        return participant;
    }

}
