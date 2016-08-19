package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedKeypair;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.meridor.perspective.sql.impl.storage.impl.StorageUtils.parseCompositeId;

@Component
public class KeypairsTableFetcher extends ProjectsBasedTableFetcher<ExtendedKeypair> {

    @Override
    protected Class<ExtendedKeypair> getBeanClass() {
        return ExtendedKeypair.class;
    }

    @Override
    public String getTableName() {
        return TableName.KEYPAIRS.getTableName();
    }

    @Override
    protected Predicate<Project> getPredicate(String id) {
        String[] pieces = parseCompositeId(id, 2);
        String projectId = pieces[0];
        String name = pieces[1];
        return p ->
                projectId.equals(p.getId()) &&
                p.getKeypairs().stream().anyMatch(k -> name.equals(k.getName()));
    }

    @Override
    protected Function<Project, Stream<ExtendedKeypair>> getConverter() {
        return ProjectConverters::projectToKeypairs;
    }
}
