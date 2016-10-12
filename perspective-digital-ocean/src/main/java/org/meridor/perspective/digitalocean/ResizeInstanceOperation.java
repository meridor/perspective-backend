package org.meridor.perspective.digitalocean;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

import static org.meridor.perspective.config.OperationType.RESIZE_INSTANCE;

@Component
public class ResizeInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiFunction<Api, Instance, Boolean> getAction() {
        return (api, instance) -> {
            try {
                api.resizeDroplet(Integer.valueOf(instance.getRealId()), instance.getFlavor().getId());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Started instance %s (%s) resize to flavor %s", instance.getName(), instance.getId(), instance.getFlavor().getName());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to resize instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{RESIZE_INSTANCE};
    }

}
