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
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.*;
import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Participant;
import org.beamproject.common.crypto.CryptoException;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.crypto.PackerException;
import org.beamproject.common.util.Base64;
import static org.beamproject.server.App.getModel;
import org.msgpack.MessageTypeException;

/**
 * This is the page that handles all incoming messages. The concrete message
 * handling is done by diverse {@link MessageHandler}s.
 */
@WebServlet(urlPatterns = {"/in/"})
public class Page extends HttpServlet {

    private final static long serialVersionUID = 1L;
    private final static String CONTENT_TYPE = "text/html;charset=UTF-8";
    public final static String MESSAGE_PARAMETER = "value";
    private final CryptoPacker packer = new CryptoPacker();
    private Message message;
    Message responseMessage;

    @Override
    final protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    final protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Incoming request: " + request.getRequestURI() + " - " + request.getParameter(MESSAGE_PARAMETER));
        try {
            decryptAndUnpack(request);
        } catch (IllegalArgumentException | PackerException | MessageTypeException | CryptoException ex) {
            sendResponse(response, SC_BAD_REQUEST);
            System.out.println("    Decryption failed: " + ex.getMessage());
            return;
        }

        if (!isMessageValid()) {
            sendResponse(response, SC_BAD_REQUEST);
            System.out.println("   Message is not valid.");
            return;
        }

        try {
            processMessage();
            sendResponse(response, SC_OK);
            System.out.println("    Message was okay and was procesed.");
        } catch (MessageException ex) {
            sendResponse(response, SC_BAD_REQUEST);
            System.out.println("    Message lead to a problem: " + ex.getMessage());
        }
    }

    private void decryptAndUnpack(HttpServletRequest request) {
        String base64Message = request.getParameter(MESSAGE_PARAMETER);
        byte[] ciphertext = Base64.decode(base64Message);

        message = packer.decryptAndUnpack(ciphertext, getModel().getServer());
    }

    private boolean isMessageValid() {
        return message != null
                && Message.VERSION.equals(message.getVersion())
                && message.getContent(TYPE) != null
                && arePublicKeysEquals(getModel().getServer(), message.getRecipient())
                && message.getContent() != null
                && !message.getContent().isEmpty();
    }

    private boolean arePublicKeysEquals(Participant first, Participant second) {
        return first.getPublicKeyAsBytes() != null
                && second.getPublicKeyAsBytes() != null
                && Arrays.equals(first.getPublicKeyAsBytes(), second.getPublicKeyAsBytes());
    }

    private void processMessage() {
        switch (message.getType()) {
            case HANDSHAKE:
                HandshakeHandler handshakeHandler = new HandshakeHandler();
                handshakeHandler.handle(message, this);
                break;
            case HEARTBEAT:
            default:
                throw new UnsupportedOperationException("Todo...");
        }
    }

    public void setResponseMessage(Message responseMessage) {
        this.responseMessage = responseMessage;
    }

    private void sendResponse(HttpServletResponse response, int statusCode) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatus(statusCode);

        if (responseMessage != null) {
            System.out.println("    Send response.");
            try (PrintWriter out = response.getWriter()) {
                out.print(Base64.encode(packer.packAndEncrypt(responseMessage)));
            }
        } else {
            System.out.println("    Do not send a response.");
        }
    }

}
