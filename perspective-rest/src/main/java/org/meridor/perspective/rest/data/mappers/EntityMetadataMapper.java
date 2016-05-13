package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.EntityMetadata;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class EntityMetadataMapper extends BaseObjectMapper<EntityMetadata> {
    @Override
    protected Map<String, Function<EntityMetadata, Object>> getColumnMapping() {
        return new HashMap<String, Function<EntityMetadata, Object>>() {
            {
                put("id", EntityMetadata::getEntityId);
                put("key", EntityMetadata::getKey);
                put("value", EntityMetadata::getValue);
            }
        };
    }

    @Override
    public Class<EntityMetadata> getInputClass() {
        return EntityMetadata.class;
    }
}
