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
package org.beamproject.server.model;

import com.google.inject.Inject;
import lombok.experimental.Delegate;
import org.beamproject.common.util.Executor;
import org.beamproject.common.util.Task;
import org.beamproject.server.carrier.ClientCarrier;

/**
 * This class implements the {@link ClientCarrierModel} interface and therefore
 * provides functionality to control a {@link ClientCarrier}.
 */
public class ClientCarrierModelImpl implements ClientCarrierModel {

    private final MainModel model;
    private final Executor executor;
    @Delegate
    private final ClientCarrier carrier;

    @Inject
    public ClientCarrierModelImpl(MainModel model, Executor executor, ClientCarrier carrier) {
        this.model = model;
        this.executor = executor;
        this.carrier = carrier;
    }

    @Override
    public void consumeMessage(final byte[] message) {
        executor.runAsync(new Task() {
            @Override
            public void run() {
                System.out.println("consumed message in client carrier: " + new String(message));
            }
        });
    }

}
