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

import org.beamproject.common.util.ConfigWriter;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

    private ConfigWriter writer;
    private Controller controller;
    private Model model;

    @Before
    public void setUp() {
        writer = createMock(ConfigWriter.class);
        App.configWriter = writer;
        ConfigTest.loadDefaultConfig();
    }

    @Test
    public void testInitBlock() {
        assertNotNull(App.configWriter);
        assertNotNull(App.controller);
        assertNotNull(App.model);
    }

    @Test
    public void testGetConfig() {
        App.config = null;
        assertNull(App.getConfig());

        ConfigTest.loadDefaultConfig();
        assertSame(App.config, App.getConfig());
    }

    @Test
    public void testGetController() {
        App.controller = null;
        assertNull(App.getController());

        controller = new Controller();
        App.controller = controller;
        assertSame(controller, App.getController());
    }

    @Test
    public void testGetModel() {
        App.model = null;
        assertNull(App.getModel());

        model = new Model();
        App.model = model;
        assertSame(model, App.getModel());
    }

    @Test
    public void testStoreConfig() {
        writer.writeConfig(App.config, Config.FOLDER, Config.FILE);
        expectLastCall();
        replay(writer);

        App.storeConfig();

        verify(writer);
    }

    /**
     * Overwrites the existing {@link Config} in {@link App} for unit testing
     * purposes.
     *
     * @param config The new config.
     */
    public static void setAppConfig(Config config) {
        App.config = config;
    }

    /**
     * Overwrites the existing {@link ConfigWriter} in {@link App} for unit
     * testing purposes.
     *
     * @param configWriter The new config writer.
     */
    public static void setAppConfigWriter(ConfigWriter configWriter) {
        App.configWriter = configWriter;
    }

    /**
     * Overwrites the existing {@link Controller} in {@link App} for unit
     * testing purposes.
     *
     * @param controller The new controller.
     */
    public static void setAppController(Controller controller) {
        App.controller = controller;
    }

    /**
     * Overwrites the existing {@link Model} in {@link App} for unit testing
     * purposes.
     *
     * @param model The new model.
     */
    public static void setAppModel(Model model) {
        App.model = model;
    }

}
