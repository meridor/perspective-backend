package org.meridor.perspective.client;

import okhttp3.ResponseBody;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

import java.util.Map;
import java.util.Set;

public interface ServiceApi {
    
    @GET("/version")
    @Headers("Accept: text/plain")
    Call<String> version();

    @GET("/ping")
    Call<ResponseBody> ping();
    
    @GET("/operations")
    Call<Map<CloudType, Set<OperationType>>> getSupportedOperations();
    
}
