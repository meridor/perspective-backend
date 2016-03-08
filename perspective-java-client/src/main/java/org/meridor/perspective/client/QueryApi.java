package org.meridor.perspective.client;

import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.Collection;

public interface QueryApi {
    
    @POST("/query")
    Call<Collection<QueryResult>> query(@Body Collection<Query> queries);
    
}
