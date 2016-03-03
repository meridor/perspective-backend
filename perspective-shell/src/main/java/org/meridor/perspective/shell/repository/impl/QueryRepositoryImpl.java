package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.List;

@Component
public class QueryRepositoryImpl implements QueryRepository {

    @Autowired
    private ApiProvider apiProvider;
    
    @Override
    public QueryResult query(Query query) {
        GenericType<ArrayList<QueryResult>> queryResultListType = new GenericType<ArrayList<QueryResult>>() {};
        List<Query> queriesList = new ArrayList<>();
        queriesList.add(query);
        GenericEntity<List<Query>> queries = new GenericEntity<List<Query>>(queriesList) {
        };
        List<QueryResult> queryResults = apiProvider.getQueryApi()
                .postXmlAs(queries, queryResultListType);
        if (queryResults.isEmpty()) {
            throw new RuntimeException("Request returned no data");
        }
        return queryResults.get(0);
    }
    
}
