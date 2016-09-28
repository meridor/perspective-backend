package org.meridor.perspective.rest.data.mappers;

import org.meridor.perspective.rest.data.beans.ExtendedQuota;
import org.meridor.perspective.sql.impl.storage.impl.BaseObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class ProjectQuotaMapper extends BaseObjectMapper<ExtendedQuota> {
    @Override
    protected Map<String, Function<ExtendedQuota, Object>> getColumnMapping() {
        return new HashMap<String, Function<ExtendedQuota, Object>>() {
            {
                put("project_id", ExtendedQuota::getProjectId);
                put("instances", ExtendedQuota::getInstances);
                put("vcpus", ExtendedQuota::getVcpus);
                put("ram", ExtendedQuota::getRam);
                put("disk", ExtendedQuota::getDisk);
                put("ips", ExtendedQuota::getIps);
                put("security_groups", ExtendedQuota::getSecurityGroups);
                put("volumes", ExtendedQuota::getVolumes);
                put("keypairs", ExtendedQuota::getKeypairs);
            }
        };
    }

    @Override
    public Class<ExtendedQuota> getInputClass() {
        return ExtendedQuota.class;
    }

    @Override
    public String getId(ExtendedQuota quota) {
        return quota.getProjectId();
    }
}
