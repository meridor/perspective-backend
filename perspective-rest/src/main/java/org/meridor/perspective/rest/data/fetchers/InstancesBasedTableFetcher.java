package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

@Component
public abstract class InstancesBasedTableFetcher<T> extends DerivedTableFetcher<Instance, T> {
    
    @Autowired
    private InstancesAware instancesAware;

    @Override
    protected Collection<Instance> getBaseEntities(Set<String> ids) {
        return instancesAware.getInstances(ids);
    }

    @Override
    protected Collection<Instance> getAllBaseEntities() {
        return instancesAware.getInstances();
    }
}
