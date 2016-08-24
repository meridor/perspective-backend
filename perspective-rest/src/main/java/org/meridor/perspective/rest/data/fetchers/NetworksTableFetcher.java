package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedNetwork;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class NetworksTableFetcher extends ProjectsBasedTableFetcher<ExtendedNetwork> {

    @Override
    protected Class<ExtendedNetwork> getBeanClass() {
        return ExtendedNetwork.class;
    }

    @Override
    public String getTableName() {
        return TableName.NETWORKS.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Project, Stream<ExtendedNetwork>> getConverter() {
        return ProjectConverters::projectToNetworks;
    }
}
