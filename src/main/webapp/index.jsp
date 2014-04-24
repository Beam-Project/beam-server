<%@page import="org.beamproject.common.network.UrlAssembler"%>
<%@page import="org.beamproject.server.App"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>beam-server</title>

        <link rel="stylesheet" href="styles/reset.css" />
        <link rel="stylesheet" href="./styles/base.css" />
    </head>
    <body>
        <div id="wrapper">
            <h1>beam-server</h1>
            <p>You can send Beam messages via this server to communicate privately.</p>
            <p>The public key is:</p>
            <p><code><%= App.getModel().getServer().getPublicKeyAsBase58()%></code></p>
            <p>You can also just click the link: <a href="<%= UrlAssembler.toUrlByServer(App.getModel().getServer())%>">beam-server</a></p>

            <p>Interact with this server via the following paths, always via <code>POST</code> and <code>GET</code></p>
            <ul>
                <li><code>/authentication</code> --  establish authentication between a client and this server</li>
                <li><code>/delivery</code> -- deliver messages to this server</li>
            </ul>
            <p>On all paths above is exactly one parameter, named <code>value</code>, expected.</p>
            <p>For more information about this server visit <a href="https://www.beamproject.org/" target="_blank">beamproject.org</a>.</p>
            <footer>
                This software is distributed under the terms of the <a href="https://gnu.org/licenses/gpl.txt" target="_blank">GPLv3 or later</a>.
            </footer>
        </div>
    </body>
</html>
