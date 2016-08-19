package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ProjectMetadata;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class ProjectMetadataTableFetcher extends ProjectsBasedTableFetcher<ProjectMetadata> {

    @Override
    protected Class<ProjectMetadata> getBeanClass() {
        return ProjectMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.PROJECT_METADATA.getTableName();
    }

    @Override
    protected Predicate<Project> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String projectId = pieces[0];
        return p -> projectId.equals(p.getId());

    }

    @Override
    protected Function<Project, Stream<ProjectMetadata>> getConverter() {
        return ProjectConverters::projectToMetadata;
    }
}
