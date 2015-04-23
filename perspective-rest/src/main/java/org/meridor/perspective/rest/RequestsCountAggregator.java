package org.meridor.perspective.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

@Aggregate
@Filter(instanceOf = CountEvent.class)
@FSM(start = CountState.class)
@Transitions(@Transit(on = CountEvent.class))
public class RequestsCountAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(RequestsCountAggregator.class);

    @AggregationKey
    public String getAggregationKey(CountEvent event) {
        return "constant";
    }

    @OnTransit
    public void onNodeEvent(CountState state, CountEvent event) {
        LOG.info("Received event with timestamp {}", event.getTimestamp());
        LOG.info("Current value is {}", state.getCount());
        state.increment();
        LOG.info("Incremented current value to {}", state.getCount());
    }
}
