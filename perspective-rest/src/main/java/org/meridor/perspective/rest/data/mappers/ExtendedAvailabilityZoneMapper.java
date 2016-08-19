package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedAvailabilityZone;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ExtendedAvailabilityZoneMapper extends BaseObjectMapper<ExtendedAvailabilityZone> {
    @Override
    protected Map<String, Function<ExtendedAvailabilityZone, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedAvailabilityZone, Object>>() {
            {
                put("project_id", ExtendedAvailabilityZone::getProjectId);
                put("name", ExtendedAvailabilityZone::getName);
            }
        };
    }

    @Override
    public Class<ExtendedAvailabilityZone> getInputClass() {
        return ExtendedAvailabilityZone.class;
    }

    @Override
    public String getId(ExtendedAvailabilityZone availabilityZone) {
        return createCompositeId(availabilityZone.getProjectId(), availabilityZone.getName());
    }
}
