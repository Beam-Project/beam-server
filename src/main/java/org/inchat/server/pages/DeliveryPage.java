/*
 * Copyright (C) 2013, 2014 inchat.org
 *
 * This file is part of inchat-server.
 *
 * inchat-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * inchat-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.inchat.server.pages;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.inchat.common.Config;
import org.inchat.common.Message;
import org.inchat.common.MessageField;
import org.inchat.common.crypto.CryptoPacker;
import org.inchat.common.util.Base64;

/**
 * This servlet takes incoming messages and processes them.
 */
@WebServlet(urlPatterns = {"/deliver"})
public class DeliveryPage extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public final static String CONTENT_TYPE = "text/html;charset=UTF-8";
    public final static String GET_MESSAGE_PARAMETER = "value";
    byte[] ciphertext;
    Message message;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String base64Message = request.getParameter(GET_MESSAGE_PARAMETER);
        ciphertext = Base64.decode(base64Message);
        decryptAndUnpack();

        sendResponse(response);
    }

    private void decryptAndUnpack() {
        CryptoPacker packer = new CryptoPacker();
        message = packer.decryptAndUnpack(ciphertext, Config.getParticipant());
    }

    private void sendResponse(HttpServletResponse response) throws IOException {
        response.setContentType(CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        System.out.println("IN: " + new String(message.getContent().get(MessageField.CNT_MSG.toString())));

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet deliver</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>It works!</h1><p>Msg:" + new String(message.getContent().get(MessageField.CNT_MSG.toString())) + " </p>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

}
