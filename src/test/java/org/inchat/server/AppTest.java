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

import java.io.File;
import java.security.KeyPair;
import org.inchat.common.Config;
import org.inchat.common.crypto.EncryptedKeyPair;
import org.inchat.common.crypto.KeyPairCryptor;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class AppTest {

    private static File configFile;
    private static Config config;

    @BeforeClass
    public static void setUpClass() {
        configFile = new File(App.CONFIG_FILENAME);
        config = new Config(App.CONFIG_FILENAME);
    }

    @AfterClass
    public static void cleanUpClass() {
        configFile.delete();
    }

    @Test
    public void testClassOnCreatingConfig() {
        assertNotNull(App.getConfig());
        assertTrue(configFile.exists());
    }

    @Test
    public void testClassOnCreatingParticipant() {
        assertNotNull(App.getParticipant());
        
        String password = config.getProperty(ServerConfigKey.keyPairPassword);
        String salt = config.getProperty(ServerConfigKey.keyPairSalt);
        String encryptedPublicKey = config.getProperty(ServerConfigKey.encryptedPublicKey);
        String encryptedPrivateKey = config.getProperty(ServerConfigKey.encryptedPrivateKey);
        EncryptedKeyPair encryptedKeyPair = new EncryptedKeyPair(encryptedPublicKey, encryptedPrivateKey, salt);
        KeyPair keyPair = KeyPairCryptor.decrypt(password, encryptedKeyPair);
        
        assertArrayEquals(keyPair.getPublic().getEncoded(), App.getParticipant().getPublicKeyAsBytes());
        assertArrayEquals(keyPair.getPrivate().getEncoded(), App.getParticipant().getPrivateKeyAsBytes());
    }

    @Test
    public void testGetConfig() {
        assertSame(App.config, App.getConfig());
    }

}
