package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedNetworkSubnet;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.createCompositeId;

@Component
public class ExtendedNetworkSubnetMapper extends BaseObjectMapper<ExtendedNetworkSubnet> {
    @Override
    protected Map<String, Function<ExtendedNetworkSubnet, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedNetworkSubnet, Object>>() {
            {
                put("id", ExtendedNetworkSubnet::getId);
                put("project_id", ExtendedNetworkSubnet::getProjectId);
                put("network_id", ExtendedNetworkSubnet::getNetworkId);
                put("name", ExtendedNetworkSubnet::getName);
                put("cidr", s -> String.format("%s/%d", s.getCidr().getAddress(), s.getCidr().getPrefixSize()));
                put("protocol_version", ExtendedNetworkSubnet::getProtocolVersion);
                put("gateway", ExtendedNetworkSubnet::getGateway);
                put("is_dhcp_enabled", ExtendedNetworkSubnet::isDHCPEnabled);
            }
        };
    }

    @Override
    public Class<ExtendedNetworkSubnet> getInputClass() {
        return ExtendedNetworkSubnet.class;
    }

    @Override
    public String getId(ExtendedNetworkSubnet subnet) {
        return createCompositeId(subnet.getProjectId(), subnet.getNetworkId(), subnet.getId());
    }
}
