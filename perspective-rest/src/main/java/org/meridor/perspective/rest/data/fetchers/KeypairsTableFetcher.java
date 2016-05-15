package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedKeypair;
import org.meridor.perspective.sql.impl.storage.impl.BaseTableFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class KeypairsTableFetcher extends BaseTableFetcher<ExtendedKeypair> {

    @Autowired
    private ProjectsAware projectsAware;

    @Override
    protected Class<ExtendedKeypair> getBeanClass() {
        return ExtendedKeypair.class;
    }

    @Override
    public String getTableName() {
        return TableName.KEYPAIRS.getTableName();
    }

    @Override
    protected Collection<ExtendedKeypair> getRawData() {
        return projectsAware.getProjects().stream()
                .flatMap(p -> p.getKeypairs().stream().map(k -> new ExtendedKeypair(p.getId(), k)))
                .collect(Collectors.toList());
    }
}
