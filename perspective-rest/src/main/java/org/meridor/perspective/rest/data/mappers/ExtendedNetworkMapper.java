package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedNetwork;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ExtendedNetworkMapper extends BaseObjectMapper<ExtendedNetwork> {
    @Override
    protected Map<String, Function<ExtendedNetwork, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedNetwork, Object>>() {
            {
                put("id", ExtendedNetwork::getId);
                put("project_id", ExtendedNetwork::getProjectId);
                put("name", ExtendedNetwork::getName);
                put("state", ExtendedNetwork::getState);
                put("is_shared", ExtendedNetwork::isShared);
            }
        };
    }

    @Override
    public Class<ExtendedNetwork> getInputClass() {
        return ExtendedNetwork.class;
    }
}
