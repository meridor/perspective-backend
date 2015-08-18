package org.meridor.perspective.mock;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.framework.EntryPoint;
import org.meridor.perspective.framework.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.*;

@Component
@Operation(cloud = MOCK, type = DELETE_INSTANCE)
public class DeleteInstanceOperation {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteInstanceOperation.class);
    
    @Autowired
    private InstancesStorage instances;
    
    @EntryPoint
    public boolean deleteInstance(Cloud cloud, Supplier<Instance> supplier) {
        Instance instance = supplier.get();
        LOG.debug("Deleting instance {}", instance);
        return instances.remove(instance);
    }
    
}
