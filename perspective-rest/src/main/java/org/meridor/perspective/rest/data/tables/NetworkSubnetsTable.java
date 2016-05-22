package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.NETWORK_SUBNETS;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@ForeignKey(columns = "network_id", table = "networks", tableColumns = "id")
@Index(columnNames = "name")
public class NetworkSubnetsTable implements Table {
    
    public String id;
    public String project_id;
    public String network_id;
    public String name;
    public String cidr;
    public Integer protocol_version;
    public String gateway;
    public Boolean is_dhcp_enabled;
    
    @Override
    public String getName() {
        return NETWORK_SUBNETS.getTableName();
    }
    
}
