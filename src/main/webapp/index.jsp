<%@page import="org.inchat.common.transfer.UrlAssembler"%>
<%@page import="org.inchat.common.Config"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>inchat-server</title>

        <link rel="stylesheet" href="styles/reset.css" />
        <link rel="stylesheet" href="./styles/base.css" />
    </head>
    <body>
        <div id="wrapper">
            <h1>inchat-server</h1>
            <p>You can send inchat messages via this server to communicate privately.</p>
            <p>The public key is:</p>
            <p><code><%= Config.getParticipant().getPublicKeyAsHex()%></code></p>
            <p>You can also just click the link: <a href="<%= UrlAssembler.toUrlByServer(Config.getParticipant())%>">inchat-server</a></p>

            <p>Interact with this server via the following paths and methods:</p>
            <ul>
                <li><code>/deliver</code> -- via <code>POST</code> or <code>GET</code> to deliver messages to this server</li>
            </ul>
            <p>On all paths above is exactly one parameter, named <code>value</code>, expected.</p>
            <p>For more information about this server visit <a href="https://www.inchat.org/" target="_blank">inchat.org</a>.</p>
            <footer>
                This software is distributed under the terms of the <a href="https://gnu.org/licenses/gpl.txt" target="_blank">GPLv3 or later</a>.
            </footer>
        </div>
    </body>
</html>
