package org.meridor.perspective.rest.data;

import org.meridor.perspective.sql.impl.table.Table;
import org.meridor.perspective.sql.impl.table.TableName;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.sql.impl.table.TableName.PROJECT_IMAGES;

@Component
public class ProjectImagesTable implements Table {
    
    public String project_id;
    public String image_id;
    
    @Override
    public TableName getName() {
        return PROJECT_IMAGES;
    }
    
}
