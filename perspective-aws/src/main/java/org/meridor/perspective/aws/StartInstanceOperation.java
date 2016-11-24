package org.meridor.perspective.aws;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.START_INSTANCE;

@Component
public class StartInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                return api.startInstance(instance.getRealId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to start instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{START_INSTANCE};
    }

}
