package org.meridor.perspective.worker.operation;

import org.meridor.perspective.config.Cloud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@Component
public abstract class AbstractEntityOperation<A, E> implements ConsumingOperation<E> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractEntityOperation.class);

    @Override
    public boolean perform(Cloud cloud, Supplier<E> supplier) {
        E entity = supplier.get();
        if (getEntityRealId(entity) == null) {
            return false;
        }
        try {
            A api = getApi(cloud, entity);
            boolean success = getAction().apply(api, entity);
            if (success) {
                LOG.debug(getSuccessMessage(entity));
                return true;
            } else {
                LOG.error(getErrorMessage(entity));
                return false;
            }
        } catch (Exception e) {
            LOG.error(getErrorMessage(entity), e);
            return false;
        }
    }

    protected abstract A getApi(Cloud cloud, E entity);

    protected abstract String getEntityRealId(E entity);

    protected abstract BiFunction<A, E, Boolean> getAction();

    protected abstract String getSuccessMessage(E entity);

    protected abstract String getErrorMessage(E entity);

}
