package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.EntityMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InstanceMetadataTableFetcher extends BaseTableFetcher<EntityMetadata> {

    @Autowired
    private InstancesAware instancesAware;

    @Override
    protected Class<EntityMetadata> getBeanClass() {
        return EntityMetadata.class;
    }

    @Override
    protected Map<String, String> getColumnRemappingRules() {
        return new HashMap<String, String>() {
            {
                put("id", "instance_id");
            }
        };
    }

    @Override
    protected TableName getTableNameConstant() {
        return TableName.INSTANCE_METADATA;
    }

    @Override
    protected Collection<EntityMetadata> getRawData() {
        return instancesAware.getInstances().stream()
                .flatMap(i ->
                        i.getMetadata().keySet().stream()
                                .map(k -> new EntityMetadata(i.getId(), k.toString().toLowerCase(), i.getMetadata().get(k)))
                )
                .collect(Collectors.toList());
    }
}
