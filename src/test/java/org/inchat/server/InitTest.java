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
import java.io.IOException;
import javax.servlet.ServletContextEvent;
import static org.easymock.EasyMock.*;
import org.inchat.common.Config;
import org.inchat.common.crypto.KeyPairStore;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class InitTest {

    private final static String CONFIG_FILE_NAME = "test-config-file.conf";
    private ServletContextEvent event;
    private Init init;

    @Before
    public void setUp() throws IOException {
        deleteConfigAndGeneratedKeyPairs();
        restoreOriginalTestConfigFile();

        event = createMock(ServletContextEvent.class);
        replay(event);

        init = new Init();
        init.CONFIG_FILENAME = CONFIG_FILE_NAME;
    }

    @After
    public void cleanUp() {
        deleteConfigAndGeneratedKeyPairs();
    }

    private void restoreOriginalTestConfigFile() throws IOException {
        Config.createDefaultConfig(CONFIG_FILE_NAME);
    }

    @Test
    public void testSetUpOfConfigFileName() {
        String expectedName = "inchat-server.conf";
        init = new Init();
        assertEquals(expectedName, init.CONFIG_FILENAME);
    }

    @Test
    public void testContextInitOnCreatingConfig() {
        File config = new File(CONFIG_FILE_NAME);

        if (config.exists()) {
            config.delete();
        }

        assertFalse(config.exists());

        init.contextInitialized(event);
        assertTrue(config.exists());
        assertNotNull(Config.getParticipant());
    }

    private void deleteConfigAndGeneratedKeyPairs() {
        File config = new File(CONFIG_FILE_NAME).getAbsoluteFile();

        if (config.exists()) {
            Config.loadConfig(CONFIG_FILE_NAME);
            config.delete();

            File publicKey = new File(Config.getProperty(Config.Key.keyPairFilename) + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
            File privateKey = new File(Config.getProperty(Config.Key.keyPairFilename) + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
            File salt = new File(Config.getProperty(Config.Key.keyPairFilename) + KeyPairStore.SALT_FILE_EXTENSION);

            if (publicKey.exists()) {
                publicKey.delete();
            }
            if (privateKey.exists()) {
                privateKey.delete();
            }
            if (salt.exists()) {
                salt.delete();
            }
        }
    }

}
