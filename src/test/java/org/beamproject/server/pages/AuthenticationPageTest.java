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
package org.beamproject.server.pages;

import org.beamproject.common.crypto.HandshakeChallenge;
import org.beamproject.server.App;
import org.beamproject.server.Session;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.junit.Before;

public class AuthenticationPageTest extends PageTest {

    private AuthenticationPage page;

    @Before
    public void setDeliveryPageUp() {
        page = new AuthenticationPage();
        basicSetup(page);
    }

    @Test
    public void testOnCompleteCyclus() {
        HandshakeChallenge challenger = new HandshakeChallenge(user);

        message = challenger.produceChallenge(server);
        setMessageToRequest();
        sendRequestAndExtractResponseToMessage();

        challenger.consumeResponse(message);
        message = challenger.produceSuccess();
        setMessageToRequest();
        sendRequestAndExtractResponseToMessage();

        Session sessionFromModel = App.getModel().getSessionByKey(challenger.getSessionKey());
        assertArrayEquals(challenger.getSessionKey(), sessionFromModel.getKey());
    }

}
