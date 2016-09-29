package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.REBUILD_INSTANCE;

@Component
public class RebuildInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<Api, Instance> getAction() {
        return (api, instance) -> api.rebuildInstance(instance.getRealId(), instance.getImage().getId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s) rebuild to image %s", instance.getName(), instance.getId(), instance.getImage().getName());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to rebuild instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBUILD_INSTANCE};
    }
    
}
