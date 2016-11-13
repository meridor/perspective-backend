package org.meridor.perspective.backend.storage;

import org.meridor.perspective.beans.Instance;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface InstancesAware {


    boolean instanceExists(String instanceId);

    Collection<Instance> getInstances();
    
    Collection<Instance> getInstances(Set<String> ids);

    Optional<Instance> getInstance(String instanceId);

    void saveInstance(Instance instance);

    boolean isInstanceDeleted(String instanceId);

    void deleteInstance(String instanceId);
    
    void addInstanceListener(EntityListener<Instance> listener);
}
