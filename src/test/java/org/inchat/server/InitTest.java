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
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class InitTest {

    private final static String CONFIG_FILE_NAME = "src/test/resources/org/inchat/server/test-config-file.conf";
    private ServletContextEvent event;
    private Init init;

    @Before
    public void setUp() throws IOException {
        cleanUp();
        restoreOriginalTestConfigFile();

        event = createMock(ServletContextEvent.class);
        replay(event);

        init = new Init();
        init.CONFIG_FILE_NAME = CONFIG_FILE_NAME;
    }

    @After
    public void cleanUp() {
        deleteConfig();
    }

    private void restoreOriginalTestConfigFile() throws IOException {
        Config.createDefaultConfig(new File(CONFIG_FILE_NAME));
    }

    @Test
    public void testSetUpOfConfigFileName() {
        String expectedName = "inchat-server.conf";
        init = new Init();
        assertEquals(expectedName, init.CONFIG_FILE_NAME);
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

    private void deleteConfig() {
        File config = new File(CONFIG_FILE_NAME);

        if (config.exists()) {
            config.delete();
        }
    }

}
