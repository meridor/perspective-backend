package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.InstanceNetwork;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class InstanceNetworksTableFetcher extends BaseTableFetcher<InstanceNetwork> {

    @Autowired
    private InstancesAware instancesAware;

    @Override
    protected Class<InstanceNetwork> getBeanClass() {
        return InstanceNetwork.class;
    }

    @Override
    public String getTableName() {
        return TableName.INSTANCE_NETWORKS.getTableName();
    }

    @Override
    protected Collection<InstanceNetwork> getRawData() {
        return instancesAware.getInstances().stream()
                .flatMap(i ->
                        i.getNetworks().stream()
                                .map(n -> new InstanceNetwork(i.getId(), n.getId()))
                )
                .collect(Collectors.toList());
    }
}
