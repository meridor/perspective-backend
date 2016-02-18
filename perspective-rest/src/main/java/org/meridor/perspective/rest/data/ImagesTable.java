package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import static org.meridor.perspective.sql.impl.table.TableName.IMAGES;
import static org.meridor.perspective.sql.impl.table.TableName.INSTANCES;

@Component
public class ImagesTable implements Table {
    
    public String id;
    public String name;
    public String cloudId;
    public String cloud_type;
    public ZonedDateTime created;
    public ZonedDateTime last_updated;
    public String state;
    public String checksum;
    
    @Override
    public TableName getName() {
        return IMAGES;
    }
    
}
