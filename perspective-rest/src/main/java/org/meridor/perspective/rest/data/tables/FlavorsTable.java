package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.FLAVORS;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@Index(columnNames = "name")
public class FlavorsTable implements Table {
    
    public String id;
    public String project_id;
    public String name;
    public String ram;
    public String vcpus;
    public String root_disk;
    public String ephemeral_disk;
    public String has_swap;
    public String is_public;
    
    @Override
    public String getName() {
        return FLAVORS.getTableName();
    }
    
}
