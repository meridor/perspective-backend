package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.openstack4j.api.OSClient;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
public class DeleteInstanceOperation extends BaseInstanceOperation {
    
    @Override
    protected BiConsumer<OSClient, Instance> getAction() {
        return (api, instance) -> api.compute().servers().delete(instance.getRealId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Deleted instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to delete instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.DELETE_INSTANCE};
    }
}
