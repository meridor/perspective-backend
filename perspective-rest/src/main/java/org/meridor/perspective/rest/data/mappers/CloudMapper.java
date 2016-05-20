package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.Cloud;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class CloudMapper extends BaseObjectMapper<Cloud> {
    @Override
    protected Map<String, Function<Cloud, Object>> getColumnMapping() {
        return new HashMap<String, Function<Cloud, Object>>() {
            {
                put("id", Cloud::getId);
                put("type", Cloud::getType);
            }
        };
    }

    @Override
    public Class<Cloud> getInputClass() {
        return Cloud.class;
    }

    @Override
    public String getId(Cloud cloud) {
        return cloud.getId();
    }
}
