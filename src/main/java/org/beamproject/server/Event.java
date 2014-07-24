package org.beamproject.server;

import com.google.common.eventbus.EventBus;
import org.beamproject.common.carrier.Carrier;
import org.beamproject.server.model.MainModel;
import org.beamproject.server.util.Config;

/**
 * Stores all the events used for the {@link EventBus}.
 */
public enum Event {

    /**
     * This event is sent by the {@link MainModel} when at least one required
     * {@link Config} entry is missing.
     */
    MISSING_CONFIG_ENTRIES,
    /**
     * This event is sent by the {@link MainModel} when the SERVER_URL in the
     * {@link Config} is not valid.
     */
    INVALID_CONFIG_SERVER_URL,
    /**
     * This event is sent by the {@link MainModel} when the command line has
     * been used the wrong way. This shows then the command line help.
     */
    INVALID_COMMAND_LINE_USAGE,
    /**
     * This event is sent by the {@link MainModel} when the command line usage
     * has led to an exception. This shows then the exceptions.
     */
    COMMAND_LINE_EXCEPTION,
    /**
     * This event is sent by the {@link MainModel} when the key pair was
     * successfully generated and stored.
     */
    KEY_PAIR_STORED,
    /**
     * This event is sent by the {@link MainModel} when there was a problem with
     * at least one {@link Carrier}.
     */
    CARRIER_EXCEPTION,
    /**
     * This event is sent by the {@link MainModel} when all {@link Carrier}s
     * have been started successfully.
     */
    CARRIERS_STARTED

}
