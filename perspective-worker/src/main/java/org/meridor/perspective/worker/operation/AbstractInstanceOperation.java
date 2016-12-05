package org.meridor.perspective.worker.operation;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@Component
public abstract class AbstractInstanceOperation<T> implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInstanceOperation.class);

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        if (instance.getRealId() == null) {
            return false;
        }
        try {
            T api = getApi(cloud, instance);
            boolean success = getAction().apply(api, instance);
            if (success) {
                LOG.debug(getSuccessMessage(instance));
                return true;
            } else {
                LOG.error(getErrorMessage(instance));
                return false;
            }
        } catch (Exception e) {
            LOG.error(getErrorMessage(instance), e);
            return false;
        }
    }

    protected abstract T getApi(Cloud cloud, Instance instance);

    protected abstract BiFunction<T, Instance, Boolean> getAction();

    protected abstract String getSuccessMessage(Instance instance);

    protected abstract String getErrorMessage(Instance instance);

}
