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

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import org.beamproject.common.Message;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.util.Base64;
import org.beamproject.server.App;
import org.beamproject.server.ConfigTest;
import org.beamproject.server.ModelTest;

public class PageTest {

    private final String PATH_BASE = "http://localhost:8080/";
    private final String SERVLET = "servlet";
    private ServletRunner runner;
    protected ServletUnitClient client;
    protected WebRequest request;
    protected WebResponse response;
    protected Participant server;
    protected Participant user;
    protected Message message;
    protected CryptoPacker packer;

    protected void basicSetup(Object page) {
        ConfigTest.loadDefaultConfig();
        server = Participant.generate();
        user = Participant.generate();
        ModelTest.setServer(server, App.getModel());
        packer = new CryptoPacker();
        message = new Message(server);

        runner = new ServletRunner();
        runner.registerServlet(SERVLET, page.getClass().getName());
        client = runner.newClient();
        request = new PostMethodWebRequest(PATH_BASE + SERVLET);
    }

    protected void setMessageToRequest() {
        byte[] ciphertext = packer.packAndEncrypt(message);
        request.setParameter(Page.GET_MESSAGE_PARAMETER, Base64.encode(ciphertext));
    }

}
