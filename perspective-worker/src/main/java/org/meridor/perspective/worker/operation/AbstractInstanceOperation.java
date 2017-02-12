package org.meridor.perspective.worker.operation;

import org.meridor.perspective.beans.Instance;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractInstanceOperation<T> extends AbstractEntityOperation<T, Instance> {
    
    @Override
    protected String getEntityRealId(Instance instance) {
        return instance.getRealId();
    }

}
