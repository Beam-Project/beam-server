package org.beamproject.server;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

public class BusFake {

    @Getter
    private final EventBus bus;
    private final List<Event> events = new LinkedList<>();

    public BusFake() {
        bus = new EventBus();
        bus.register(this);
    }

    @Subscribe
    public void consumeEvent(Event event) {
        events.add(event);
    }

    public Event getNextEvent() {
        if (events.isEmpty()) {
            throw new IllegalStateException("There is no event available.");
        }

        return events.remove(0);
    }

    public void verify() {
        if (!events.isEmpty()) {
            String message = "The bus is not empty:";

            for (Event event : events) {
                message += " " + event;
            }

            message += ".";

            throw new IllegalStateException(message);
        }
    }

}
