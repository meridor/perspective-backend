package org.meridor.perspective.aws;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Component
public class DeleteInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                return api.deleteInstance(instance.getRealId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Deleted instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage(Instance instance) {
        return String.format("Failed to delete instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.DELETE_INSTANCE};
    }
}
