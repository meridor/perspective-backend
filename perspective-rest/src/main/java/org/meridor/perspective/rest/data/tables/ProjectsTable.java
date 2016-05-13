package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECTS;

@Component
public class ProjectsTable implements Table {
    
    public String id;
    public String name;
    public String cloud_id;
    public String cloud_type;
    public String last_updated;
    
    @Override
    public String getName() {
        return PROJECTS.getTableName();
    }
    
}
