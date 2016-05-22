package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.KEYPAIRS;

@Component
@ForeignKey(columns = "project_id", table = "projects", tableColumns = "id")
@Index(columnNames = "name")
public class KeypairsTable implements Table {
    
    public String name;
    public String project_id;
    public String fingerprint;
    public String public_key;
    
    @Override
    public String getName() {
        return KEYPAIRS.getTableName();
    }
    
}
