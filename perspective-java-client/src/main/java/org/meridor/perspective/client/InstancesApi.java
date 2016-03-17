package org.meridor.perspective.client;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Instance;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.Collection;

public interface InstancesApi {
    
    @GET("/instances/{id}")
    Call<Instance> getById(@Path("id") String instanceId);
    
    @POST("/instances")
    Call<Collection<Instance>> launch(@Body Collection<Instance> instances);

    @PUT("/instances/reboot")
    Call<ResponseBody> reboot(@Body Collection<String> instanceIds);

    @PUT("/instances/hard-reboot")
    Call<ResponseBody> hardReboot(@Body Collection<String> instanceIds);

    @POST("/instances/delete")
    Call<ResponseBody> delete(@Body Collection<String> instanceIds);
    
}
