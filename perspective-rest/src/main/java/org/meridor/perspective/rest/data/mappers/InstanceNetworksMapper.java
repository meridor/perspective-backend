package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.InstanceNetwork;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class InstanceNetworksMapper extends BaseObjectMapper<InstanceNetwork> {
    @Override
    protected Map<String, Function<InstanceNetwork, Object>> getColumnMapping() {
        return new HashMap<String, Function<InstanceNetwork, Object>>() {
            {
                put("instance_id", InstanceNetwork::getInstanceId);
                put("network_id", InstanceNetwork::getNetworkId);
            }
        };
    }

    @Override
    public Class<InstanceNetwork> getInputClass() {
        return InstanceNetwork.class;
    }
}
