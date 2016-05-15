package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedNetworkSubnet;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class NetworkSubnetsTableFetcher extends BaseTableFetcher<ExtendedNetworkSubnet> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ExtendedNetworkSubnet> getBeanClass() {
        return ExtendedNetworkSubnet.class;
    }

    @Override
    public String getTableName() {
        return TableName.NETWORK_SUBNETS.getTableName();
    }

    @Override
    protected Collection<ExtendedNetworkSubnet> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(p ->
                        p.getNetworks().stream().flatMap(n ->
                                n.getSubnets().stream()
                                        .map(s -> new ExtendedNetworkSubnet(p.getId(), n.getId(), s))
                        )
                )
                .collect(Collectors.toList());
    }
}
