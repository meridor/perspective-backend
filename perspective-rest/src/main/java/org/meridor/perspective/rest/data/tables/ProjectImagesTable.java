package org.meridor.perspective.rest.data.tables;

import org.meridor.perspective.sql.impl.table.Table;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.rest.data.TableName.PROJECT_IMAGES;

@Component
public class ProjectImagesTable implements Table {
    
    public String project_id;
    public String image_id;
    
    @Override
    public String getName() {
        return PROJECT_IMAGES.getTableName();
    }
    
}
