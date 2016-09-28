package org.meridor.perspective.docker;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.operation.ConsumingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class DeleteInstanceOperation implements ConsumingOperation<Instance> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteInstanceOperation.class);

    @Autowired
    private ApiProvider apiProvider;

    @Override
    public boolean perform(Cloud cloud, Supplier<Instance> supplier) {
        try {
            Api api = apiProvider.getApi(cloud);
            Instance instance = supplier.get();
            api.deleteContainer(instance.getRealId());
            LOG.debug("Deleted instance {} ({})", instance.getName(), instance.getId());
            return true;
        } catch (Exception e) {
            LOG.error("Failed to delete instance", e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{OperationType.DELETE_INSTANCE};
    }
}
