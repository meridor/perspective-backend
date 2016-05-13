package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedNetwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class NetworksTableFetcher extends BaseTableFetcher<ExtendedNetwork> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ExtendedNetwork> getBeanClass() {
        return ExtendedNetwork.class;
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.NETWORKS;
    }

    @Override
    protected Collection<ExtendedNetwork> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(p -> p.getNetworks().stream().map(n -> new ExtendedNetwork(p.getId(), n)))
                .collect(Collectors.toList());
    }
}
