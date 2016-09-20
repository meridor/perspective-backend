package org.meridor.perspective.client;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ServiceApi {
    
    @GET("/version")
    @Headers("Accept: text/plain")
    Call<String> version();

    @GET("/ping")
    Call<ResponseBody> ping();
    
}
