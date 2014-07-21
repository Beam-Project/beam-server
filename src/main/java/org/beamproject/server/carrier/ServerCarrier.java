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

import org.beamproject.server.model.ServerCarrierModel;

/**
 * A {@link ServerCarrier} is used to connect to servers of the Beam network in
 * order to deliver and receive messages and share network status information.
 *
 * @see ServerCarrierImpl
 * @see ServerCarrierModel
 * @see Carrier
 */
public interface ServerCarrier extends Carrier<ServerCarrierModel> {

}
