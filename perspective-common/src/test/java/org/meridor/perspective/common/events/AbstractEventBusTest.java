package org.meridor.perspective.common.events;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AbstractEventBusTest {

    @Rule
    public Timeout timeout = new Timeout(500, TimeUnit.MILLISECONDS);

    @Test
    public void testFireAsync() throws Exception {
        final AtomicBoolean fired = new AtomicBoolean(false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        EventListener<Object> listener = evt -> {
            fired.set(true);
            countDownLatch.countDown();
        };
        EventBus eventBus = createEventBus();
        eventBus.addListener(Object.class, listener);
        assertThat(fired.get(), is(false));
        eventBus.fireAsync(new Object());
        countDownLatch.await();
        assertThat(fired.get(), is(true));
    }

    @Test
    public void testFire() {
        final AtomicBoolean fired = new AtomicBoolean(false);
        EventListener<Object> listener = evt -> fired.set(true);
        EventBus eventBus = createEventBus();
        eventBus.addListener(Object.class, listener);
        eventBus.fire(new Object());
        assertThat(fired.get(), is(true));
    }

    private static EventBus createEventBus() {
        return new AbstractEventBus() {
            @Override
            protected int getParallelConsumers() {
                return 1;
            }
        };

    }
    
}