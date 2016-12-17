package org.meridor.perspective.digitalocean;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.RENAME_INSTANCE;

@Component
public class RenameInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                api.renameDroplet(Integer.valueOf(instance.getRealId()), instance.getName());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Renamed instance %s to %s", instance.getId(), instance.getName());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to rename instance %s to %s", instance.getId(), instance.getName());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{RENAME_INSTANCE};
    }

}
