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
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.IOException;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.MSG;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.util.Base64;
import static org.beamproject.server.App.getModel;
import org.beamproject.server.ConfigTest;
import org.beamproject.server.ModelTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class PageTest {

    protected final String PATH_BASE = "http://localhost:8080/";
    protected final String SERVLET = "servlet";
    private ServletRunner runner;
    protected ServletUnitClient client;
    protected WebRequest request;
    protected WebResponse response;
    protected Participant server;
    protected Participant user;
    protected Message message;
    protected CryptoPacker packer;
    private Page page;

    @Before
    public void setPageUp() {
        ConfigTest.loadDefaultConfig();
        page = new Page();
        server = Participant.generate();
        user = Participant.generate();
        ModelTest.setServer(server, getModel());
        packer = new CryptoPacker();
        message = new Message(HANDSHAKE, server);

        runner = new ServletRunner();
        runner.registerServlet(SERVLET, page.getClass().getName());
        client = runner.newClient();
        request = new PostMethodWebRequest(PATH_BASE + SERVLET);
    }

    @Test
    public void testOnEmptyRequest() {
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnEmptyValue() {
        request.setParameter(Page.MESSAGE_PARAMETER, "");
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnShortValues() {
        for (int i = 0; i < 10000; i += 100) {
            request.setParameter(Page.MESSAGE_PARAMETER, "" + i);
            sendRequestAndCatchException(HTTP_BAD_REQUEST);
        }

        String value = "";
        for (int i = 0; i < 500; i++) {
            request.setParameter(Page.MESSAGE_PARAMETER, value);
            sendRequestAndCatchException(HTTP_BAD_REQUEST);
            value += (char) (i % 128); // Try all ASCII values 
        }
    }

    @Test
    public void testOnEmptyMessage() {
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithOnlyVersion() {
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithWrongVersion() {
        message.setVersion("2.7182");
        message.putContent(MSG, "hello".getBytes());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithEmptyVersion() {
        message.setVersion("");
        message.putContent(MSG, "hello".getBytes());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithWrongRecipient() {
        message.setRecipient(Participant.generate());
        message.putContent(MSG, "hello".getBytes());
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testOnMessageWithoutContent() {
        setMessageToRequest();
        sendRequestAndCatchException(HTTP_BAD_REQUEST);
    }

    @Test
    public void testSetResponseMessage() {
        Message responseMessage = new Message(BLANK, Participant.generate());
        page.setResponseMessage(responseMessage);
        assertSame(responseMessage, page.responseMessage);
    }

    protected void setMessageToRequest() {
        byte[] ciphertext = packer.packAndEncrypt(message);
        request.setParameter(Page.MESSAGE_PARAMETER, Base64.encode(ciphertext));
    }

    protected void sendRequestAndCatchException(int statusCode) {
        try {
            response = client.getResponse(request);
            fail("This should have thrown an exception.");
        } catch (HttpException ex) {
            assertEquals(statusCode, ex.getResponseCode());
        } catch (IOException | SAXException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

    protected void sendRequestAndExtractResponseToMessage() {
        try {
            response = client.getResponse(request);
            byte[] ciphertext = Base64.decode(response.getText());
            message = packer.decryptAndUnpack(ciphertext, user);
        } catch (IOException | SAXException | HttpException ex) {
            fail("Unexpected exception: " + ex.getMessage());
        }
    }

}
