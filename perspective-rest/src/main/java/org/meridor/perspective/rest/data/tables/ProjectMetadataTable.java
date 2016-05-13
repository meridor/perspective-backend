package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECT_METADATA;

@Component
public class ProjectMetadataTable implements Table {
    
    public String project_id;
    public String key;
    public String value;
    
    @Override
    public String getName() {
        return PROJECT_METADATA.getTableName();
    }
    
}
