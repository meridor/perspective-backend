package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.PROJECT_METADATA;

@Component
public class ProjectMetadataTable implements Table {
    
    public String project_id;
    public String key;
    public String value;
    
    @Override
    public TableName getName() {
        return PROJECT_METADATA;
    }
    
}
