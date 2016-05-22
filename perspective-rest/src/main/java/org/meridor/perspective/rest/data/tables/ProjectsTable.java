package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECTS;

@Component
@Index(columnNames = "name")
@ForeignKey(columns = "cloud_id", table = "clouds", tableColumns = "id")
@ForeignKey(columns = "cloud_type", table = "clouds", tableColumns = "type")
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
