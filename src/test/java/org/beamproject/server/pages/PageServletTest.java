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

import com.meterware.httpunit.HttpException;
import java.io.IOException;
import static java.net.HttpURLConnection.*;
import static org.beamproject.common.MessageField.*;
import org.beamproject.common.Participant;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class PageServletTest extends PageTest {

    private PageServlet page;

    @Before
    public void setDeliveryPageUp() {
        page = new PageServlet();
        basicSetup(page);
    }

    @Test
    public void testOnEmptyRequest() {
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnEmptyValue() {
        request.setParameter(Page.MESSAGE_PARAMETER, "");
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnShortValues() {
        for (int i = 0; i < 10000; i += 100) {
            request.setParameter(Page.MESSAGE_PARAMETER, "" + i);
            sendResponseAndCatchException(HTTP_BAD_REQUEST);
        }

        String value = "";
        for (int i = 0; i < 500; i++) {
            request.setParameter(Page.MESSAGE_PARAMETER, value);
            sendResponseAndCatchException(HTTP_BAD_REQUEST);
            value += (char) (i % 128); // Try all ASCII values 
        }
    }

    @Test
    public void testOnEmptyMessage() {
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithOnlyVersion() {
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithWrongVersion() {
        message.setVersion("2.7182");
        message.putContent(CNT_MSG, "hello".getBytes());
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithEmptyVersion() {
        message.setVersion("");
        message.putContent(CNT_MSG, "hello".getBytes());
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithWrongRecipient() {
        message.setRecipient(Participant.generate());
        message.putContent(CNT_MSG, "hello".getBytes());
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithoutContent() {
        setMessageToRequest();
        sendResponseAndCatchException(HTTP_BAD_REQUEST);
    }

    private void sendResponseAndCatchException(int statusCode) {
        try {
            response = client.getResponse(request);
            fail("This should have thrown an exception.");
        } catch (HttpException ex) {
            assertEquals(statusCode, ex.getResponseCode());
        } catch (IOException | SAXException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    @Test
    public void testOnCorrectMessage() throws IOException, SAXException {
        message.putContent(CNT_MSG, "hello".getBytes());
        setMessageToRequest();
        response = client.getResponse(request);
        assertEquals(HTTP_OK, response.getResponseCode());
    }

}
