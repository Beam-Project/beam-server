<%@page import="org.beamproject.server.App"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>beam-server</title>

        <link rel="stylesheet" href="styles/reset.css" />
        <link rel="stylesheet" href="./styles/base.css" />
    </head>
    <body>
        <div id="wrapper">
            <h1>Beam Server</h1>
            <p>You can send Beam messages via this server to communicate privately.</p>

            <p class="linkText">Use this <a href="<%= App.getModel().getServer().getAddress()%>">Beam server address</a> link to configure your client.</p>

            <p>For more information about this server, visit <a href="https://www.beamproject.org/" target="_blank">beamproject.org</a>.</p>
            <footer>
                This software is distributed under the terms of the <a href="https://gnu.org/licenses/gpl.txt" target="_blank">GPLv3 or later</a>.
            </footer>
        </div>
    </body>
</html>
