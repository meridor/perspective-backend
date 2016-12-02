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
    public void testFire() throws Exception {
        final AtomicBoolean fired = new AtomicBoolean(false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Object event = new Object();
        EventListener<Object> listener = evt -> {
            fired.set(true);
            countDownLatch.countDown();
        };
        EventBus eventBus = new AbstractEventBus() {
            @Override
            protected int getParallelConsumers() {
                return 1;
            }
        };
        eventBus.addListener(Object.class, listener);
        assertThat(fired.get(), is(false));
        eventBus.fire(event);
        countDownLatch.await();
        assertThat(fired.get(), is(true));
    }

}