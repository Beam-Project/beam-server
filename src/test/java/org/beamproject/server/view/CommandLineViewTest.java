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
package org.beamproject.server.view;

import java.util.Properties;
import org.beamproject.server.BusFake;
import org.beamproject.server.model.MainModel;
import org.beamproject.server.util.Config;
import static org.easymock.EasyMock.*;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;

public class CommandLineViewTest {

    private MainModel model;
    private BusFake busFake;
    private Config config;
    private CommandLineView view;

    @Before
    public void setUp() {
        model = createMock(MainModel.class);
        busFake = new BusFake();
        config = new Config(new Properties());
        view = new CommandLineView(model, busFake.getBus(), config);
    }

    @After
    public void verifyBus() {
        busFake.verify();
    }

    @Test
    public void testParseOnOption_g() {
        model.generateKeyPair("file");
        expectLastCall();
        replay(model);

        view.parse(new String[]{"-g", "file"});

        verify(model);
    }

    @Test
    public void testParseOnBootstrapping() {
        model.bootstrap();
        expectLastCall();
        replay(model);

        view.parse(new String[]{});

        verify(model);
    }

}
