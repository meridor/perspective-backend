package org.meridor.perspective.rest.data.fetchers;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.rest.data.TableName;
import org.meridor.perspective.rest.data.beans.ExtendedKeypair;
import org.meridor.perspective.rest.data.converters.ProjectConverters;
import org.springframework.stereotype.Component;

import java.util.function.Function;
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
    protected String getBaseEntityId(String id) {
        String[] pieces = parseCompositeId(id, 2);
        return pieces[0];
    }

    @Override
    protected Function<Project, Stream<ExtendedKeypair>> getConverter() {
        return ProjectConverters::projectToKeypairs;
    }
}
