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
package org.beamproject.server.carrier;

import org.beamproject.common.User;
import org.beamproject.server.model.ClientCarrierModel;

/**
 * A {@link ClientCarrier} is used to connect to clients (mobile devices,
 * desktop clients, etc.) in order to provide access to the {@link User}s
 * account, notify all devices about new messages, etc..
 *
 * @see ClientCarrierImpl
 * @see ClientCarrierModel
 * @see Carrier
 */
public interface ClientCarrier extends Carrier<ClientCarrierModel> {

    /**
     * Binds the given {@link User} to the given {@code topic}. Messages, whose
     * recipient is the given {@link User}, are then being published to this
     * {@code topic}.
     * <p>
     * The topic has to be known by all clients (mobile devices, desktop client,
     * etc.) of this {@link User} in order to subscribe to it. 
     * <p>
     * If the user is already bound to a topic, that binding will be
     * overwritten.
     *
     * @param user The user to bind.
     * @param topic The topic to bind.
     */
    public void bindUserToTopic(User user, String topic);

    /**
     * Unbinds the given {@link User} from a possibly bound topic.
     *
     * @param user The user to unbind.
     */
    public void unbindUser(User user);
}
