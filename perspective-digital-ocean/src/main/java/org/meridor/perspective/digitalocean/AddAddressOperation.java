package org.meridor.perspective.digitalocean;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.HARD_REBOOT_INSTANCE;

@Component
public class AddAddressOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                //TODO: should be also able to add existing IP via api.addAddress(id, ip)
                Integer dropletId = Integer.valueOf(instance.getRealId());
                api.addAddress(dropletId);
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format(
                "Added floating IP %s to instance %s (%s)",
                instance.getAddresses().get(instance.getAddresses().size() - 1),
                instance.getName(),
                instance.getId()
        );
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to add floating IP to instance %s (%s)",  instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{HARD_REBOOT_INSTANCE};
    }

}
