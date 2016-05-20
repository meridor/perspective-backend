package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedFlavor;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class FlavorsTableFetcher extends BaseTableFetcher<ExtendedFlavor> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ExtendedFlavor> getBeanClass() {
        return ExtendedFlavor.class;
    }

    @Override
    public String getTableName() {
        return TableName.FLAVORS.getTableName();
    }

    @Override
    protected Collection<ExtendedFlavor> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(ProjectConverters::projectToFlavors)
                .collect(Collectors.toList());
    }
}
