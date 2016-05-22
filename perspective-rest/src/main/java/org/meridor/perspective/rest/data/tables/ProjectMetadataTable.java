package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECT_METADATA;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@Index(columnNames = {"project_id", "key"})
public class ProjectMetadataTable implements Table {
    
    public String project_id;
    public String key;
    public String value;
    
    @Override
    public String getName() {
        return PROJECT_METADATA.getTableName();
    }
    
}
