package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ProjectImage;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ProjectImageMapper extends BaseObjectMapper<ProjectImage> {
    @Override
    protected Map<String, Function<ProjectImage, Object>> getColumnMapping() {
        return new HashMap<String, Function<ProjectImage, Object>>() {
            {
                put("project_id", ProjectImage::getProjectId);
                put("image_id", ProjectImage::getImageId);
            }
        };
    }

    @Override
    public Class<ProjectImage> getInputClass() {
        return ProjectImage.class;
    }

    @Override
    public String getId(ProjectImage projectImage) {
        return createCompositeId(projectImage.getProjectId(), projectImage.getImageId());
    }
}
