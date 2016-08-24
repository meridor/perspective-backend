package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fetcher for the tables that are derived from base objects in storage such as project, instance or image.
 * For derived entities we need to convert ids to match base collection against.
 * @param <T> type of the object this table is derived from
 */
@Component
public abstract class DerivedTableFetcher<B, T> extends BaseTableFetcher<T> {

    @Autowired
    private ObjectMapperAware objectMapperAware;

    /**
     *   Converts derived entity ids to base entity ids 
     */
    protected abstract String getBaseEntityId(String id);
    
    /**
     * Knows how to fetch base entities by ids
     */
    protected abstract Collection<B> getBaseEntities(Set<String> ids);

    /**
     * Knows how to fetch all base entities
     */
    protected abstract Collection<B> getAllBaseEntities();

    /**
     * Knows how to convert base entity to derived one
     */
    protected abstract Function<B, Stream<T>> getConverter();

    @Override
    protected Collection<T> getRawEntities(Set<String> ids) {
        Assert.isTrue(!ids.isEmpty(), "IDs can not be empty at this point");
        ObjectMapper<T> objectMapper = objectMapperAware.get(getBeanClass());
        Set<String> baseEntityIds = ids.stream()
                .map(this::getBaseEntityId)
                .collect(Collectors.toSet());
        return getBaseEntities(baseEntityIds).stream()
                .flatMap(getConverter())
                //Base entity can contain some derived entities that do not match given ids
                .filter(e -> ids.contains(objectMapper.getId(e)))
                .collect(Collectors.toList());
    }

    @Override
    protected Collection<T> getAllRawEntities() {
        return getAllBaseEntities().stream()
                .flatMap(getConverter())
                .collect(Collectors.toList());
    }
}
