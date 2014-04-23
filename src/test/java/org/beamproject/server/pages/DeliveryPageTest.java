/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-SERVLET.
 *
 * beam-SERVLET is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-SERVLET is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.server.pages;

import java.io.IOException;
import static org.beamproject.common.MessageField.CNT_MSG;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DeliveryPageTest extends PageTest {

    private DeliveryPage page;

    @Before
    public void setDeliveryPageUp() {
        page = new DeliveryPage();
        basicSetup(page);
    }

    @After
    public void printStatus() {
        if (response != null) {
            System.out.println("Status: " + response.getResponseCode() + " " + response.getResponseMessage());
        }
    }

    @Test
    public void testDeliveryPageConstructor() {
        assertNotNull(page.packer);
    }

    @Test
    public void testSendingMessage() throws IOException, SAXException {
        message.putContent(CNT_MSG, "hello".getBytes());
        setMessageToRequest();
        response = client.getResponse(request);
        System.out.println(response.getText());
    }

}
