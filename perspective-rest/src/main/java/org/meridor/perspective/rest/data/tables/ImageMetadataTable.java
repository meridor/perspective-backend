package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.annotation.ForeignKey;
import org.meridor.perspective.sql.impl.table.annotation.Index;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.IMAGE_METADATA;

@Component
@ForeignKey(columns = "image_id", table = "images", tableColumns = "id")
@Index(columnNames = {"image_id", "key"})
public class ImageMetadataTable implements Table {
    
    public String image_id;
    public String key;
    public String value;
    
    @Override
    public String getName() {
        return IMAGE_METADATA.getTableName();
    }
    
}
