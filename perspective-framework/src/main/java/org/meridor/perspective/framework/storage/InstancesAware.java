package org.meridor.perspective.framework.storage;

import org.meridor.perspective.beans.Instance;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface InstancesAware {


    boolean instanceExists(String instanceId);

    Collection<Instance> getInstances();
    
    Collection<Instance> getInstances(Set<String> ids);
    
    Collection<Instance> getInstances(Predicate<Instance> predicate);

    Optional<Instance> getInstance(String instanceId);

    void saveInstance(Instance instance);

    boolean isInstanceDeleted(String instanceId);

    void deleteInstance(String instanceId);
    
    void addInstanceListener(EntityListener<Instance> listener);
}
