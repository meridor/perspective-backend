package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ImageMapper extends BaseObjectMapper<Image> {
    @Override
    protected Map<String, Function<Image, Object>> getColumnMapping() {
        return new HashMap<String, Function<Image, Object>>() {
            {
                put("id", Image::getId);
                put("real_id", Image::getRealId);
                put("name", Image::getName);
                put("cloud_id", Image::getCloudId);
                put("cloud_type", i -> i.getCloudType().value());
                put("last_updated", i -> i.getTimestamp().format(DATE_FORMATTER));
                put("created", i -> i.getCreated().format(DATE_FORMATTER));
                put("state", i -> i.getState().value());
                put("checksum", Image::getChecksum);
            }
        };
    }

    @Override
    public Class<Image> getInputClass() {
        return Image.class;
    }

    @Override
    public String getId(Image image) {
        return image.getId();
    }
}
