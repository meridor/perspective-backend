package org.meridor.perspective.client;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceApi {
    
    @GET("/version")
    Call<String> version();
    
}
