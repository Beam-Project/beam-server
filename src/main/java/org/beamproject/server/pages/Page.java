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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.*;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.util.Base64;
import static org.beamproject.server.App.getModel;

/**
 * This is a basic page that can be extended to concrete pages.
 * <p>
 * Here, all basic checks and operations are done. The extending classes will
 * only have to take care of the effective work with the {@link Message}s.
 */
public abstract class Page extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final static String CONTENT_TYPE = "text/html;charset=UTF-8";
    protected final static String GET_MESSAGE_PARAMETER = "value";
    protected CryptoPacker packer = new CryptoPacker();
    protected Message message;

    @Override
    final protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    final protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        decryptAndUnpack(request);
        processMessage();
        sendResponse(response);
    }

    private void decryptAndUnpack(HttpServletRequest request) {
        String base64Message = request.getParameter(GET_MESSAGE_PARAMETER);
        byte[] ciphertext = Base64.decode(base64Message);

        message = packer.decryptAndUnpack(ciphertext, getModel().getServer());
    }

    /**
     * This method processes the received {@link Message}.
     */
    abstract protected void processMessage();

    private void sendResponse(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        System.out.println("IN: " + new String(message.getContent(CNT_MSG)));
    }

}
