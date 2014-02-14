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
package org.inchat.server;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.inchat.common.Config;
import org.inchat.common.Participant;
import org.inchat.common.crypto.EccKeyPairGenerator;
import org.inchat.common.crypto.KeyPairStore;

/**
 * {@link Init} operates as an init service which is invoked on every startup of
 * the web application.<p>
 * {@link Init} does the following at every startup of the web application:
 * <ul>
 * <li>Load {@link Participant} with the key pair or create and store a new one
 * at the configured location using the configured password.</li>
 * </ul>
 */
public class Init implements ServletContextListener {

    String CONFIG_FILE_NAME = "inchat-server.conf";
    String keyPairPassword;
    String keyPairFilename;

    /**
     * This method will be invoked at every startup of the web application.
     *
     * @param event The event, sent by the servlet container.
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        loadConfigFileOrCreateOne();
        readImportanntConfigEntries();

        if (isKeyPairExisting()) {
            loadParticipant();
        } else {
            createAndStoreNewParticipant();
        }
    }

    private void loadConfigFileOrCreateOne() {
        File defaultFile = new File(CONFIG_FILE_NAME);

        if (defaultFile.exists()) {
            Config.loadConfigFile(defaultFile);
        } else {
            Config.createDefaultConfig(defaultFile);
            Config.loadConfigFile(defaultFile);
        }
    }

    private void readImportanntConfigEntries() {
        keyPairFilename = Config.getProperty(Config.Keys.keyPairFilename.toString());
        keyPairPassword = Config.getProperty(Config.Keys.keyPairPassword.toString());
    }

    private boolean isKeyPairExisting() {
        File privateKeyFile = new File(keyPairFilename + KeyPairStore.PRIVATE_KEY_FILE_EXTENSION);
        File publicKeyFile = new File(keyPairFilename + KeyPairStore.PUBILC_KEY_FILE_EXTENSION);
        File saltFile = new File(keyPairFilename + KeyPairStore.SALT_FILE_EXTENSION);

        if (!privateKeyFile.exists()) {
            return false;
        }

        if (!publicKeyFile.exists()) {
            return false;
        }

        return saltFile.exists();
    }

    private void loadParticipant() {
        KeyPairStore store = new KeyPairStore(keyPairPassword, keyPairFilename);
        Participant participant = new Participant(store.readKeys());
        Config.setParticipant(participant);
    }

    private void createAndStoreNewParticipant() {
        Participant participant = new Participant(EccKeyPairGenerator.generate());
        Config.setParticipant(participant);

        KeyPairStore store = new KeyPairStore(keyPairPassword, keyPairFilename);
        store.storeKeys(participant.getKeyPair());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do nothing
    }

}
