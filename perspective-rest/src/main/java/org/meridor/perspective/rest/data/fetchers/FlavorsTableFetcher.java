package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedFlavor;
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
    protected TableName getTableNameConstant() {
        return TableName.FLAVORS;
    }

    @Override
    protected Collection<ExtendedFlavor> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(p -> p.getFlavors().stream().map(f -> new ExtendedFlavor(p.getId(), f)))
                .collect(Collectors.toList());
    }
}
