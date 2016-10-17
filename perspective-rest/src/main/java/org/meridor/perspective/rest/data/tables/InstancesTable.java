package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.INSTANCES;

@Component
@ForeignKey(columns = "cloud_id", table = "clouds", tableColumns = "id")
@ForeignKey(columns = "cloud_type", table = "clouds", tableColumns = "type")
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@ForeignKey(columns = "image_id", table = "images", tableColumns = "id")
@ForeignKey(columns = "flavor_id", table = "flavors", tableColumns = "id")
@Index(columnNames = "name")
@Index(columnNames = "state")
public class InstancesTable implements Table {
    
    public String id;
    public String real_id;
    public String name;
    public String cloud_id;
    public String cloud_type;
    public String project_id;
    public String flavor_id;
    public String image_id;
    public String state;
    public String last_updated;
    public String created;
    public String availability_zone;
    public String addresses; //Networks are provided via instance_networks table
    public String fqdn;
    
    @Override
    public String getName() {
        return INSTANCES.getTableName();
    }
    
}
