package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.KEYPAIRS;

@Component
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
