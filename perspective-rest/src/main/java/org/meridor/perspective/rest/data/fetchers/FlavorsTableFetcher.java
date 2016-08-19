package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedFlavor;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class FlavorsTableFetcher extends ProjectsBasedTableFetcher<ExtendedFlavor> {

    @Override
    protected Class<ExtendedFlavor> getBeanClass() {
        return ExtendedFlavor.class;
    }

    @Override
    public String getTableName() {
        return TableName.FLAVORS.getTableName();
    }


    @Override
    protected Predicate<Project> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String projectId = pieces[0];
        String flavorId = pieces[1];
        return p ->
                projectId.equals(p.getId()) &&
                p.getFlavors().stream().anyMatch(f -> flavorId.equals(f.getId()));
    }

    @Override
    protected Function<Project, Stream<ExtendedFlavor>> getConverter() {
        return ProjectConverters::projectToFlavors;
    }
}
