package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class InstanceMapper extends BaseObjectMapper<Instance> {
    @Override
    protected Map<String, Function<Instance, Object>> getColumnMapping() {
        return new HashMap<String, Function<Instance, Object>>() {
            {
                put("id", Instance::getId);
                put("real_id", Instance::getRealId);
                put("name", Instance::getName);
                put("cloud_id", Instance::getCloudId);
                put("cloud_type", i -> i.getCloudType().value());
                put("project_id", Instance::getProjectId);
                put("flavor_id", i -> (i.getFlavor() != null) ? i.getFlavor().getId() : null);
                put("image_id", i -> (i.getImage() != null) ? i.getImage().getId() : null);
                put("state", i -> i.getState().value());
                put("last_updated", i -> i.getTimestamp().format(DATE_FORMATTER));
                put("created", i -> i.getCreated().format(DATE_FORMATTER));
                put("availability_zone", i -> (i.getAvailabilityZone() != null) ? i.getAvailabilityZone().getName() : null);
                put("addresses", i -> i.getAddresses().stream().collect(Collectors.joining("\n")));
            }
        };
    }

    @Override
    public Class<Instance> getInputClass() {
        return Instance.class;
    }
}
