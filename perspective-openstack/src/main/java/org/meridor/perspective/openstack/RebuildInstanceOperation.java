package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.REBUILD_INSTANCE;

@Component
public class RebuildInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> api.rebuildInstance(instance.getRealId(), instance.getImage().getRealId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started rebuilding instance %s (%s) to image %s", instance.getName(), instance.getId(), instance.getImage().getName());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to rebuild instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBUILD_INSTANCE};
    }
    
}
