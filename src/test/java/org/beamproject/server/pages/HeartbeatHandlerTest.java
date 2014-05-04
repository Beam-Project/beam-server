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
import org.beamproject.common.Participant;
import org.beamproject.common.Session;
import org.beamproject.common.util.Timestamps;
import org.beamproject.server.AppTest;
import org.beamproject.server.Model;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class HeartbeatHandlerTest {

    private final byte[] KEY = "key".getBytes();
    private Model model;
    private Page page;
    private HeartbeatHandler handler;
    private Message message;
    private Participant user, server;

    @Before
    public void setUp() {
        user = Participant.generate();
        server = Participant.generate();
        model = new Model();
        model.addSession(user, KEY);
        AppTest.setAppModel(model);
        page = new Page();
        handler = new HeartbeatHandler();
        message = new Message(HEARTBEAT, server);
        message.putContent(HBKEY, KEY);
        message.putContent(HBTS, Timestamps.getIso8601UtcTimestamp());
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMissingKey() {
        message.getContent().remove(HBKEY.toString());
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnNullKey() {
        message.getContent().put(HBKEY.toString(), null);
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnNonExistingKey() {
        message.putContent(HBKEY, "other key");
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMissingTimestamp() {
        message.getContent().remove(HBTS.toString());
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnNullTimestamp() {
        message.getContent().put(HBTS.toString(), null);
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnMalformattedTimestamp() {
        message.putContent(HBTS, "today, at 12:42:12, UTC");
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnTimestampInFuture() {
        DateTime futureTimestamp = Timestamps.getUtcTimestamp().plus(Minutes.ONE);
        message.putContent(HBTS, Timestamps.formatter.print(futureTimestamp));
        handler.handle(message, page);
    }

    @Test(expected = MessageException.class)
    public void testHandleOnTimestampTooFarInPast() {
        DateTime pastTimestamp = Timestamps.getUtcTimestamp().minus(Minutes.ONE);
        message.putContent(HBTS, Timestamps.formatter.print(pastTimestamp));
        handler.handle(message, page);
    }

    @Test
    public void testHandle() {
        configurePageMockForResponeMessage();
        DateTime now = Timestamps.getUtcTimestamp();
        Session session = model.getSessionByKey(KEY);
        session.setLastestInteractionTime(now.minus(Minutes.THREE).minus(Seconds.TWO));

        assertEquals(now.getHourOfDay(), session.getLatestInteractionTime().getHourOfDay());
        assertThat(now.getMinuteOfDay(), not(equalTo(session.getLatestInteractionTime().getMinuteOfDay())));
        assertThat(now.getSecondOfDay(), not(equalTo(session.getLatestInteractionTime().getSecondOfDay())));

        handler.handle(message, page);
        assertEquals(now.getHourOfDay(), session.getLatestInteractionTime().getHourOfDay());
        assertEquals(now.getMinuteOfDay(), session.getLatestInteractionTime().getMinuteOfDay());
        assertEquals(now.getSecondOfDay(), session.getLatestInteractionTime().getSecondOfDay());

        verify(page);
    }

    private void configurePageMockForResponeMessage() {
        page = createMock(Page.class);
        page.setResponseMessage(anyObject(Message.class));
        expectLastCall().andDelegateTo(new Page() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setResponseMessage(Message response) {
                assertEquals(Message.VERSION, response.getVersion());
                assertEquals(user, response.getRecipient());
                assertArrayEquals(HEARTBEAT.getBytes(), response.getContent(TYPE));
                assertArrayEquals(KEY, response.getContent(HBKEY));
                assertAlmostCurrentTimestamp(response.getContent(HBTS));
            }

            private void assertAlmostCurrentTimestamp(byte[] timestampBytes) {
                DateTime now = Timestamps.getUtcTimestamp();
                DateTime timestamp = Timestamps.parseIso8601UtcTimestamp(new String(timestampBytes));
                assertEquals(now.getYear(), timestamp.getYear());
                assertEquals(now.getMonthOfYear(), timestamp.getMonthOfYear());
                assertEquals(now.getDayOfMonth(), timestamp.getDayOfMonth());
                assertEquals(now.getHourOfDay(), timestamp.getHourOfDay());
                assertEquals(now.getMinuteOfHour(), timestamp.getMinuteOfHour());
                assertEquals(now.getSecondOfMinute(), timestamp.getSecondOfMinute(), 1d);
            }
        });
        replay(page);
    }

}
