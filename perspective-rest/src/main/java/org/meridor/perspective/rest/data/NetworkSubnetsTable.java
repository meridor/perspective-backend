package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.NETWORK_SUBNETS;

@Component
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
