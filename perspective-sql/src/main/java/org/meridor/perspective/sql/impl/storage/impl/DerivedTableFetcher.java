package org.meridor.perspective.sql.impl.storage.impl;

import org.meridor.perspective.sql.impl.storage.ObjectMapper;
import org.meridor.perspective.sql.impl.storage.ObjectMapperAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fetcher for the tables that are derived from base objects in storage such as project, instance or image.
 * For derived entities we need to convert ids to a predicate to match base collection against.

 * @param <T> type of the object this table is derived from
 */
@Component
public abstract class DerivedTableFetcher<B, T> extends BaseTableFetcher<T> {

    @Autowired
    private ObjectMapperAware objectMapperAware;

    /**
     * Transforms id to predicate for the object this table is derived from
     */
    protected abstract Predicate<B> getPredicate(String id);
    
    /**
     * Knows how to fetch entities using a single predicate that considers
     * all passed ids.
     */
    protected abstract Function<Predicate<B>, Collection<B>> getPredicateFetcher();

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
        Predicate<B> globalPredicate = ids.stream()
                .map(this::getPredicate)
                .reduce(
                        (l, r) -> (e -> l.test(e) || r.test(e))
                ).get();
        ObjectMapper<T> objectMapper = objectMapperAware.get(getBeanClass());
        return getPredicateFetcher().apply(globalPredicate).stream()
                .flatMap(getConverter())
                .filter(e -> ids.contains(objectMapper.getId(e))) //Base entity can contain some derived entities that do not match given ids
                .collect(Collectors.toList());
    }

    @Override
    protected Collection<T> getAllRawEntities() {
        return getAllBaseEntities().stream()
                .flatMap(getConverter())
                .collect(Collectors.toList());
    }
}
