package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.CLOUDS;
import static org.meridor.perspective.sql.impl.table.TableName.KEYPAIRS;

@Component
public class KeypairsTable implements Table {
    
    public String name;
    public String project_id;
    public String fingerprint;
    public String public_key;
    
    @Override
    public TableName getName() {
        return KEYPAIRS;
    }
    
}
