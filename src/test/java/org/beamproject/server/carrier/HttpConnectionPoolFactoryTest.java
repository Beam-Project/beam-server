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
package org.beamproject.server.carrier;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class HttpConnectionPoolFactoryTest {

    private HttpConnectionPoolFactory factory;

    @Before
    public void setUp() {
        factory = new HttpConnectionPoolFactory();
    }

    @Test
    public void testCreate() throws Exception {
        assertNotNull(factory.create());
    }

    @Test
    public void testWrap() throws Exception {
        assertNotNull(factory.wrap(factory.create()));
    }

}
