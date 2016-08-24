package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedAvailabilityZone;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class AvailabilityZonesTableFetcher extends ProjectsBasedTableFetcher<ExtendedAvailabilityZone> {

    @Override
    protected Class<ExtendedAvailabilityZone> getBeanClass() {
        return ExtendedAvailabilityZone.class;
    }

    @Override
    public String getTableName() {
        return TableName.AVAILABILITY_ZONES.getTableName();
    }

    @Override
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Project, Stream<ExtendedAvailabilityZone>> getConverter() {
        return ProjectConverters::projectToAvailabilityZones;
    }

}
