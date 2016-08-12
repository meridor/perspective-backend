package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.actions.RebuildOptions;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.REBUILD_INSTANCE;

@Component
public class RebuildInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<OSClient, Instance> getAction() {
        return (api, instance) -> api.compute().servers().rebuild(
                instance.getRealId(),
                RebuildOptions.create()
                        .image(instance.getImage().getId())
                        .name(instance.getName())
        );
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
