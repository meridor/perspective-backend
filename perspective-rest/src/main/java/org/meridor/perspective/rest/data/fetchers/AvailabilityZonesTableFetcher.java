package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedAvailabilityZone;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class AvailabilityZonesTableFetcher extends BaseTableFetcher<ExtendedAvailabilityZone> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ExtendedAvailabilityZone> getBeanClass() {
        return ExtendedAvailabilityZone.class;
    }

    @Override
    public String getTableName() {
        return TableName.AVAILABILITY_ZONES.getTableName();
    }

    @Override
    protected Collection<ExtendedAvailabilityZone> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(ProjectConverters::projectToAvailabilityZones)
                .collect(Collectors.toList());
    }
}
