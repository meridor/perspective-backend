package org.meridor.perspective.shell.common.repository.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.meridor.perspective.shell.common.misc.HumanReadableException;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.meridor.perspective.shell.common.repository.ApiProvider.processRequestOrException;

@Component
public class QueryRepositoryImpl implements QueryRepository {

    private final ApiProvider apiProvider;
    
    private LoadingCache<Query, QueryResult> queryCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS) //This can be converted to shell setting
            .build(
                new CacheLoader<Query, QueryResult>() {
                    public QueryResult load(Query key) throws Exception {
                        return processQuery(key);
                    }
                }
            );
    
    @Autowired
    public QueryRepositoryImpl(ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public QueryResult query(Query query) {
        try {
            return queryCache.get(query);
        } catch (Exception e) {
            throw new HumanReadableException(e);
        }
    }
    
    private QueryResult processQuery(Query query) {
        return processRequestOrException(() -> {
            List<Query> queries = new ArrayList<>();
            queries.add(query);
            Call<Collection<QueryResult>> call = apiProvider.getQueryApi()
                    .query(queries);
            Collection<QueryResult> body = call.execute().body();
            if (body == null || body.isEmpty()) {
                throw new HumanReadableException("request returned no data");
            }
            return new ArrayList<>(body).get(0);
        });
    }
    
}
