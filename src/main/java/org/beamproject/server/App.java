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
import org.aeonbits.owner.ConfigFactory;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.EccKeyPairGenerator;
import org.beamproject.common.crypto.EncryptedKeyPair;
import org.beamproject.common.crypto.KeyPairCryptor;
import org.beamproject.common.util.ConfigWriter;

/**
 * This class provides static access to global instances, such as the
 * {@link Participant} or the {@link Config} classes.<p>
 * This is not a singleton since it is much easier to test the class this way.
 */
public class App {

    static ConfigWriter configWriter;
    static Config config;
    static Participant participant;

    static {
        loadConfig();
        loadParticipant();
    }

    private static void loadConfig() {
        configWriter = new ConfigWriter();
        config = ConfigFactory.create(Config.class);
    }

    private static void loadParticipant() {
        if (isEncryptedKeyPairStored()) {
            readAndDecryptParticipantFromConfig();
        } else {
            generateParticipant();
            storeConfig();
        }
    }

    private static boolean isEncryptedKeyPairStored() {
        return config.encryptedPublicKey() != null && config.encryptedPrivateKey() != null;
    }

    private static void readAndDecryptParticipantFromConfig() {
        EncryptedKeyPair encryptedKeyPair = new EncryptedKeyPair(config.encryptedPublicKey(), config.encryptedPrivateKey(), config.keyPairSalt());
        KeyPair keyPair = KeyPairCryptor.decrypt(config.keyPairPassword(), encryptedKeyPair);
        participant = new Participant(keyPair);
    }

    private static void generateParticipant() {
        participant = new Participant(EccKeyPairGenerator.generate());

        EncryptedKeyPair encryptedKeyPair = KeyPairCryptor.encrypt(config.keyPairPassword(), participant.getKeyPair());
        config.setProperty("keyPairSalt", encryptedKeyPair.getSalt());
        config.setProperty("encryptedPublicKey", encryptedKeyPair.getEncryptedPublicKey());
        config.setProperty("encryptedPrivateKey", encryptedKeyPair.getEncryptedPrivateKey());
    }

    public static Participant getParticipant() {
        return participant;
    }

    public static void storeConfig() {
        configWriter.writeConfig(config, Config.FOLDER, Config.FILE);
    }

}
