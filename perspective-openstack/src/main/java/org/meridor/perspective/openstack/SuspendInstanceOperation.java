package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.SUSPEND_INSTANCE;

@Component
public class SuspendInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<Api, Instance> getAction() {
        return (api, instance) -> api.suspendInstance(instance.getRealId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Suspended instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to suspend instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{SUSPEND_INSTANCE};
    }
    
}
