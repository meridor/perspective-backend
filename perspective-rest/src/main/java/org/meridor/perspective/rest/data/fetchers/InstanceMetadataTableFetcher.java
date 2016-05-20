package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.InstanceMetadata;
import org.meridor.perspective.rest.data.converters.InstanceConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class InstanceMetadataTableFetcher extends BaseTableFetcher<InstanceMetadata> {

    @Autowired
    private InstancesAware instancesAware;

    @Override
    protected Class<InstanceMetadata> getBeanClass() {
        return InstanceMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.INSTANCE_METADATA.getTableName();
    }

    @Override
    protected Collection<InstanceMetadata> getRawData() {
        return instancesAware.getInstances().stream()
                .flatMap(InstanceConverters::instanceToMetadata)
                .collect(Collectors.toList());
    }
}
