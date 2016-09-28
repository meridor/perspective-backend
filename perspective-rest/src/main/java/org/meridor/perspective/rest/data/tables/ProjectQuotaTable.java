package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECT_QUOTA;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
public class ProjectQuotaTable implements Table {
    
    public String project_id;
    public String instances;
    public String vcpus;
    public String ram;
    public String disk;
    public String ips;
    public String security_groups;
    public String volumes;
    public String keypairs;
    
    @Override
    public String getName() {
        return PROJECT_QUOTA.getTableName();
    }
    
}
