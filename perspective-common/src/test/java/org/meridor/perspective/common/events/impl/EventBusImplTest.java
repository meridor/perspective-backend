package org.meridor.perspective.common.events.impl;

import org.junit.Test;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.common.events.EventListener;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EventBusImplTest {

    @Test
    public void testFire() {
        final AtomicBoolean fired = new AtomicBoolean(false);
        Object event = new Object();
        EventListener<Object> listener = evt -> fired.set(true);
        EventBus eventBus = new EventBusImpl();
        eventBus.addListener(Object.class, listener);
        assertThat(fired.get(), is(false));
        eventBus.fire(event);
        assertThat(fired.get(), is(true));
    }

}