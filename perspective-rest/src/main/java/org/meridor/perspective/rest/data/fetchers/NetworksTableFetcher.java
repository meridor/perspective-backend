package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedNetwork;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
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
    protected Predicate<Project> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String projectId = pieces[0];
        String networkId = pieces[1];
        return p ->
                projectId.equals(p.getId()) &&
                p.getNetworks().stream().anyMatch(n -> networkId.equals(n.getId()));
    }

    @Override
    protected Function<Project, Stream<ExtendedNetwork>> getConverter() {
        return ProjectConverters::projectToNetworks;
    }
}
