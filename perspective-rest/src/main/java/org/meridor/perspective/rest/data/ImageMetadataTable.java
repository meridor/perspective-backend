package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.IMAGE_METADATA;

@Component
public class ImageMetadataTable implements Table {
    
    public String image_id;
    public String key;
    public String value;
    
    @Override
    public TableName getName() {
        return IMAGE_METADATA;
    }
    
}
