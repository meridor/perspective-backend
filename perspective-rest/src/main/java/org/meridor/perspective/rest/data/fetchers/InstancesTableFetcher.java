package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.rest.data.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class InstancesTableFetcher extends BaseTableFetcher<Instance> {

    @Autowired
    private InstancesAware instancesAware;

    @Override
    protected Class<Instance> getBeanClass() {
        return Instance.class;
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.INSTANCES;
    }

    @Override
    protected Collection<Instance> getRawData() {
        return instancesAware.getInstances();
    }
}
