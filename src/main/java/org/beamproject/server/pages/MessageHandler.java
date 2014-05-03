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

/**
 * Defines a common interface for all different {@link Message} types. Concrete
 * handler implement this interface to handle one specific message type.
 */
public interface MessageHandler {

    /**
     * Handles the given {@link Message}.
     * <p>
     * The message has been validated in the following aspects:
     * <ul>
     * <li>Version</li>
     * <li>Recipient (is equals the local participant)</li>
     * <li>Type is existing and a valid one</li>
     * </ul>
     *
     * @param message The message to process by the concrete
     * {@link MessageHandler} implementation.
     * @param page The invoking {@link Page}. A response can be given via this
     * page.
     * @throws MessageException If anything goes wrong during handling the
     * message.
     */
    public void handle(Message message, Page page);
}
