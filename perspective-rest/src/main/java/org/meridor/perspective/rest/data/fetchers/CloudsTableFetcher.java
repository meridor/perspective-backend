package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class CloudsTableFetcher extends BaseTableFetcher<Project> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<Project> getBeanClass() {
        return Project.class;
    }

    @Override
    protected Map<String, String> getColumnRemappingRules() {
        return new HashMap<String, String>() {
            {
                put("cloud_id", "id");
                put("cloud_type", "type");
            }
        };
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.CLOUDS;
    }

    @Override
    protected Collection<Project> getRawData() {
        return projectsAware.getProjects();
    }
}
