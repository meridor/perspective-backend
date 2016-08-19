package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ImageMetadata;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ImageMetadataMapper extends BaseObjectMapper<ImageMetadata> {
    @Override
    protected Map<String, Function<ImageMetadata, Object>> getColumnMapping() {
        return new HashMap<String, Function<ImageMetadata, Object>>() {
            {
                put("image_id", ImageMetadata::getId);
                put("key", ImageMetadata::getKey);
                put("value", ImageMetadata::getValue);
            }
        };
    }

    @Override
    public Class<ImageMetadata> getInputClass() {
        return ImageMetadata.class;
    }

    @Override
    public String getId(ImageMetadata metadata) {
        return createCompositeId(metadata.getId(), metadata.getKey());
    }
}
