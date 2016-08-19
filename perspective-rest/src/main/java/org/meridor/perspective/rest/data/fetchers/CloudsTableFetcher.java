package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.Cloud;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
public class CloudsTableFetcher extends ProjectsBasedTableFetcher<Cloud> {

    @Override
    protected Class<Cloud> getBeanClass() {
        return Cloud.class;
    }

    @Override
    public String getTableName() {
        return TableName.CLOUDS.getTableName();
    }

    @Override
    protected Predicate<Project> getPredicate(String id) {
        return p -> p.getCloudId().equals(id);
    }

    @Override
    protected Function<Project, Stream<Cloud>> getConverter() {
        return ProjectConverters::projectToCloud;
    }

}
