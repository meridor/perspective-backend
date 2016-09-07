package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.RebootType;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.REBOOT_INSTANCE;

@Component
public class RebootInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<OSClient, Instance> getAction() {
        return (api, instance) -> api.compute().servers().reboot(instance.getRealId(), RebootType.SOFT);
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Rebooted instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to reboot instance";
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBOOT_INSTANCE};
    }
}
