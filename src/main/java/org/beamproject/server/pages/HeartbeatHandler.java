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

import org.beamproject.common.Message;
import static org.beamproject.common.MessageField.ContentField.*;
import static org.beamproject.common.MessageField.ContentField.TypeValue.*;
import org.beamproject.common.Session;
import org.beamproject.common.util.Timestamps;
import static org.beamproject.server.App.getConfig;
import static org.beamproject.server.App.getModel;
import org.joda.time.DateTime;

/**
 * Handles an incoming HEARTBEAT {@link Message} and prepares a response if
 * everything is okay.
 */
public class HeartbeatHandler implements MessageHandler {

    private Message message;
    private Page page;
    private Session session;
    private DateTime now;

    /**
     * Verifies the given {@link Message} on:
     * <ul>
     * <li>Is field HBKEY set, not null and a valid session?</li>
     * <li>Is field HBTS set, not null, a valid ISO 8601 timestamp (UTC) and
     * within the allowed deviation?</li>
     * </ul>
     *
     * Afterwards, the local {@link Session} is updated and a response is sent,
     * or, if a problem occurs, this message is ignored and no response is sent
     * back. When the latter happens, the session will <b>not</b> be terminated.
     *
     * @param message The message of the type HEARTBEAT.
     * @param page The invoking {@link Page}.
     * @throws MessageException If a problem occurs.
     */
    @Override
    public void handle(Message message, Page page) {
        this.message = message;
        this.page = page;

        verifyFieldFormalities();
        verifyTimestampIsWithinAllowedDeviation();
        verifySessionKeyIsActive();

        updateSession();
        createResponseMessage();
    }

    private void verifyFieldFormalities() {
        if (!message.containsContent(HBKEY)) {
            throw new MessageException("The message does not contain the required field " + HBKEY + ".");
        }

        if (message.getContent(HBKEY) == null) {
            throw new MessageException("The session key is not valid.");
        }

        if (!message.containsContent(HBTS)) {
            throw new MessageException("The message does not contain the required field " + HBTS + ".");
        }

        if (message.getContent(HBTS) == null
                || !Timestamps.isValidIso8601UtcTimestamp(new String(message.getContent(HBTS)))) {
            throw new MessageException("The timestamp is not valid.");
        }
    }

    private void verifyTimestampIsWithinAllowedDeviation() {
        now = Timestamps.getUtcTimestamp();
        DateTime heartbeatTimestamp = Timestamps.parseIso8601UtcTimestamp(new String(message.getContent(HBTS)));
        long deltaInMilliseconds = now.getMillis() - heartbeatTimestamp.getMillis();

        if (deltaInMilliseconds < 0
                || deltaInMilliseconds > getConfig().maxHeartbeatTimestampDeviationInMilliseconds()) {
            throw new MessageException("The timestamp is not within allowd derivation.");
        }

    }

    private void verifySessionKeyIsActive() {
        if (!getModel().isSessionExistingByKey(message.getContent(HBKEY))) {
            throw new MessageException("The session key is not valid.");
        }
    }

    private void updateSession() {
        session = getModel().getSessionByKey(message.getContent(HBKEY));
        session.setLastestInteractionTime(now);
    }

    private void createResponseMessage() {
        Message response = new Message(HEARTBEAT, session.getRemoteParticipant());
        response.putContent(HBKEY, session.getKey());
        response.putContent(HBTS, Timestamps.getIso8601UtcTimestamp());
        page.setResponseMessage(response);
    }
}
