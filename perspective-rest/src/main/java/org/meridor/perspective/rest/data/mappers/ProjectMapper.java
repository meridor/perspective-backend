package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ProjectMapper extends BaseObjectMapper<Project> {
    @Override
    protected Map<String, Function<Project, Object>> getColumnMapping() {
        return new HashMap<String, Function<Project, Object>>() {
            {
                put("id", Project::getId);
                put("name", Project::getName);
                put("cloud_id", Project::getCloudId);
                put("cloud_type", p -> p.getCloudType().value());
                put("last_updated", p -> p.getTimestamp().format(DATE_FORMATTER));
            }
        };
    }

    @Override
    public Class<Project> getInputClass() {
        return Project.class;
    }
}
