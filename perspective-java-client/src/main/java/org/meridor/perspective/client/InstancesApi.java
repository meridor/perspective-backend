package org.meridor.perspective.client;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Instance;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Collection;
import java.util.Map;

public interface InstancesApi {
    
    @GET("/instances/{id}")
    Call<Instance> getById(@Path("id") String instanceId);
    
    @POST("/instances")
    Call<ResponseBody> launch(@Body Collection<Instance> instances);

    @PUT("/instances/start")
    Call<ResponseBody> start(@Body Collection<String> instanceIds);
    
    @PUT("/instances/shutdown")
    Call<ResponseBody> shutdown(@Body Collection<String> instanceIds);
    
    @PUT("/instances/pause")
    Call<ResponseBody> pause(@Body Collection<String> instanceIds);
    
    @PUT("/instances/resume")
    Call<ResponseBody> resume(@Body Collection<String> instanceIds);
    
    @PUT("/instances/suspend")
    Call<ResponseBody> suspend(@Body Collection<String> instanceIds);
    
    @PUT("/instances/resize/{flavorId}")
    Call<ResponseBody> resize(@Path("flavorId") String flavorId, @Body Collection<String> instanceIds);

    @PUT("/instances/rename")
    Call<ResponseBody> rename(@Body Map<String, String> newNames);
    
    @PUT("/instances/rebuild/{imageId}")
    Call<ResponseBody> rebuild(@Path("imageId") String imageId, @Body Collection<String> instanceIds);
    
    @PUT("/instances/reboot")
    Call<ResponseBody> reboot(@Body Collection<String> instanceIds);

    @PUT("/instances/hard-reboot")
    Call<ResponseBody> hardReboot(@Body Collection<String> instanceIds);

    @POST("/instances/delete")
    Call<ResponseBody> delete(@Body Collection<String> instanceIds);
    
}
