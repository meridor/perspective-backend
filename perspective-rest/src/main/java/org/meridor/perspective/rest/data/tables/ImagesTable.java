package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.IMAGES;

@Component
public class ImagesTable implements Table {
    
    public String id;
    public String real_id;
    public String name;
    public String cloud_id;
    public String cloud_type;
    public String created;
    public String last_updated;
    public String state;
    public String checksum;
    
    @Override
    public String getName() {
        return IMAGES.getTableName();
    }
    
}
