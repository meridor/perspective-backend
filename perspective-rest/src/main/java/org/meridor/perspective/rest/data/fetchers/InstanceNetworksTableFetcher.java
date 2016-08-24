package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.InstanceNetwork;
import org.meridor.perspective.rest.data.converters.InstanceConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class InstanceNetworksTableFetcher extends InstancesBasedTableFetcher<InstanceNetwork> {

    @Override
    protected Class<InstanceNetwork> getBeanClass() {
        return InstanceNetwork.class;
    }

    @Override
    public String getTableName() {
        return TableName.INSTANCE_NETWORKS.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Instance, Stream<InstanceNetwork>> getConverter() {
        return InstanceConverters::instanceToNetworks;
    }
}
