package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.sql.impl.storage.impl.DerivedTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public abstract class InstancesBasedTableFetcher<T> extends DerivedTableFetcher<Instance, T> {
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Override
    protected Function<Predicate<Instance>, Collection<Instance>> getPredicateFetcher() {
        return predicate -> instancesAware.getInstances(predicate);
    }

    @Override
    protected Collection<Instance> getAllBaseEntities() {
        return instancesAware.getInstances();
    }
}
