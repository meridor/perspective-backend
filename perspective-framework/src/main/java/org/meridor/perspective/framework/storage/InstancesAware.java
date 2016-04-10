package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Instance;

import java.util.Collection;
import java.util.Optional;

public interface InstancesAware {


    boolean instanceExists(String instanceId);

    Collection<Instance> getInstances();

    Optional<Instance> getInstance(String instanceId);

    void saveInstance(Instance instance);

    boolean isInstanceDeleted(String instanceId);

    void deleteInstance(String instanceId);

}
