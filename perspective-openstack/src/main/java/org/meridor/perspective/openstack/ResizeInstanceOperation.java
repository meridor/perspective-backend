package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.RESIZE_INSTANCE;

@Component
public class ResizeInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<Api, Instance> getAction() {
        return (api, instance) -> api.resizeInstance(instance.getRealId(), instance.getFlavor().getId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s) resize to flavor %s", instance.getName(), instance.getId(), instance.getFlavor().getName());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to resize instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{RESIZE_INSTANCE};
    }
    
}
