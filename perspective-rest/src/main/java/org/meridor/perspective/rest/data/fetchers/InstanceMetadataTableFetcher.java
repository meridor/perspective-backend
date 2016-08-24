package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.InstanceMetadata;
import org.meridor.perspective.rest.data.converters.InstanceConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class InstanceMetadataTableFetcher extends InstancesBasedTableFetcher<InstanceMetadata> {

    @Override
    protected Class<InstanceMetadata> getBeanClass() {
        return InstanceMetadata.class;
    }

    @Override
    public String getTableName() {
        return TableName.INSTANCE_METADATA.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Instance, Stream<InstanceMetadata>> getConverter() {
        return InstanceConverters::instanceToMetadata;
    }
}
