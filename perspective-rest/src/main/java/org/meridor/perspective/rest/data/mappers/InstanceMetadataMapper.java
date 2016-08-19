package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.InstanceMetadata;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class InstanceMetadataMapper extends BaseObjectMapper<InstanceMetadata> {
    @Override
    protected Map<String, Function<InstanceMetadata, Object>> getColumnMapping() {
        return new HashMap<String, Function<InstanceMetadata, Object>>() {
            {
                put("instance_id", InstanceMetadata::getId);
                put("key", InstanceMetadata::getKey);
                put("value", InstanceMetadata::getValue);
            }
        };
    }

    @Override
    public Class<InstanceMetadata> getInputClass() {
        return InstanceMetadata.class;
    }

    @Override
    public String getId(InstanceMetadata metadata) {
        return createCompositeId(metadata.getId(), metadata.getKey());
    }
}
