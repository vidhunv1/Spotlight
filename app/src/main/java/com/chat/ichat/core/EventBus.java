package com.chat.ichat.core;

import com.squareup.otto.Bus;

/**
 * Created by vidhun on 29/01/17.
 */

public class EventBus {
    private static EventBus instance;

    private Bus bus;

    public EventBus(Bus bus) {
        this.bus = bus;
    }

    public static EventBus getInstance() {
        if(instance == null) {
            throw new IllegalStateException();
        }

        return instance;
    }

    public static void init(Bus bus) {
        instance = new EventBus(bus);
    }

    public Bus getBus() {
        return bus;
    }
}
