package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.REBOOT_INSTANCE;

@Component
public class RebootInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> api.rebootInstance(instance.getRealId());
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Rebooted instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to reboot instance %s (%s)", instance.getName(), instance.getId());
    }
    
    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{REBOOT_INSTANCE};
    }
}
