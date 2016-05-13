package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.EntityMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProjectMetadataTableFetcher extends BaseTableFetcher<EntityMetadata> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<EntityMetadata> getBeanClass() {
        return EntityMetadata.class;
    }

    @Override
    protected Map<String, String> getColumnRemappingRules() {
        return new HashMap<String, String>() {
            {
                put("id", "project_id");
            }
        };
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.PROJECT_METADATA;
    }

    @Override
    protected Collection<EntityMetadata> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(p ->
                        p.getMetadata().keySet().stream()
                                .map(k -> new EntityMetadata(p.getId(), k.toString().toLowerCase(), p.getMetadata().get(k)))
                )
                .collect(Collectors.toList());
    }
}
