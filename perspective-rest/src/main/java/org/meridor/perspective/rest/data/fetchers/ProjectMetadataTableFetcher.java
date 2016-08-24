package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ProjectMetadata;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
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
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Project, Stream<ProjectMetadata>> getConverter() {
        return ProjectConverters::projectToMetadata;
    }
}
