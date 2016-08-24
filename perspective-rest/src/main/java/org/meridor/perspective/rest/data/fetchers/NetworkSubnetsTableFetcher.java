package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedNetworkSubnet;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class NetworkSubnetsTableFetcher extends ProjectsBasedTableFetcher<ExtendedNetworkSubnet> {

    @Override
    protected Class<ExtendedNetworkSubnet> getBeanClass() {
        return ExtendedNetworkSubnet.class;
    }

    @Override
    public String getTableName() {
        return TableName.NETWORK_SUBNETS.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 3);
        return pieces[0];
    }

    @Override
    protected Function<Project, Stream<ExtendedNetworkSubnet>> getConverter() {
        return ProjectConverters::projectToNetworkSubnets;
    }
}
