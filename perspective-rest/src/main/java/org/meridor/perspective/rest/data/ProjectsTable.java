package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static org.meridor.perspective.sql.impl.table.TableName.CLOUDS;
import static org.meridor.perspective.sql.impl.table.TableName.PROJECTS;

@Component
public class ProjectsTable implements Table {
    
    public String id;
    public String name;
    public String cloud_id;
    public String cloud_type;
    public String last_updated;
    
    @Override
    public TableName getName() {
        return PROJECTS;
    }
    
}
