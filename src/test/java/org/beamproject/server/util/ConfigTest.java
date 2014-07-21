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

import java.util.Properties;
import static org.beamproject.server.util.Config.Key.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ConfigTest {

    private Config config;

    @Before
    public void setUp() {
        config = new Config(new Properties());
    }

    @Test
    public void testSetAndGet() {
        config.set(SERVER_URL, "myUrl");
        assertEquals("myUrl", config.get(SERVER_URL));
        assertEquals("myUrl", config.getProperties().getProperty(SERVER_URL.toString()));
    }

    @Test
    public void testContains() {
        assertFalse(config.contains(SERVER_URL));
        config.set(SERVER_URL, "myUrl");
        assertTrue(config.contains(SERVER_URL));
    }

}
