package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.meridor.perspective.shell.repository.ApiProvider.processRequestOrException;

@Component
public class QueryRepositoryImpl implements QueryRepository {

    @Autowired
    private ApiProvider apiProvider;
    
    @Override
    public QueryResult query(Query query) {
        return processRequestOrException(() -> {
            List<Query> queries = new ArrayList<>();
            queries.add(query);
            Call<Collection<QueryResult>> call = apiProvider.getQueryApi()
                    .query(queries);
            Collection<QueryResult> body = call.execute().body();
            if (body == null || body.isEmpty()) {
                throw new RuntimeException("Request returned no data");
            }
            return new ArrayList<>(body).get(0);
        });
    }
    
}
