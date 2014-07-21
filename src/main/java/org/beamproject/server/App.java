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
package org.beamproject.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import org.beamproject.server.view.CommandLineView;

/**
 * The main class of this application.
 */
public class App {

    public final static String NAME = "beam-server";
    public final static String POM_VERSION = "0.0.1"; // Do NOT change this, Maven replaces it.
    public final static String WEBSITE = "https://www.beamproject.org/";
    public final static String CONFIG_DIRECTORY_PATH = System.getProperty("user.home") + File.separator + ".beam" + File.separator;
    public final static String CONFIG_PATH = CONFIG_DIRECTORY_PATH + "server.conf";
    private static CommandLineView commandLineView;

    public static void main(String args[]) {
        Injector appInjector = Guice.createInjector(new AppModule());
        commandLineView = appInjector.getInstance(CommandLineView.class);
        commandLineView.parse(args);
    }
}
