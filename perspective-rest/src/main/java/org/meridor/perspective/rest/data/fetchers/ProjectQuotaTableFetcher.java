package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedQuota;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class ProjectQuotaTableFetcher extends ProjectsBasedTableFetcher<ExtendedQuota> {

    @Override
    protected Class<ExtendedQuota> getBeanClass() {
        return ExtendedQuota.class;
    }

    @Override
    public String getTableName() {
        return TableName.PROJECT_QUOTA.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        return id;
    }

    @Override
    protected Function<Project, Stream<ExtendedQuota>> getConverter() {
        return ProjectConverters::projectToQuota;
    }
}
