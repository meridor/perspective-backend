package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ProjectMetadata;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ProjectMetadataMapper extends BaseObjectMapper<ProjectMetadata> {
    @Override
    protected Map<String, Function<ProjectMetadata, Object>> getColumnMapping() {
        return new HashMap<String, Function<ProjectMetadata, Object>>() {
            {
                put("project_id", ProjectMetadata::getId);
                put("key", ProjectMetadata::getKey);
                put("value", ProjectMetadata::getValue);
            }
        };
    }

    @Override
    public Class<ProjectMetadata> getInputClass() {
        return ProjectMetadata.class;
    }

    @Override
    public String getId(ProjectMetadata metadata) {
        return metadata.getId() + metadata.getKey();
    }
}
