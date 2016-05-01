package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.FLAVORS;

@Component
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
