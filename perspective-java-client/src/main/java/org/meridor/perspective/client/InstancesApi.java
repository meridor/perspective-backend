package org.meridor.perspective.client;

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
    Call<Response> reboot(@Body Collection<String> instanceIds);

    @PUT("/instances/hard-reboot")
    Call<Response> hardReboot(@Body Collection<String> instanceIds);

    @PUT("/instances/delete")
    Call<Response> delete(@Body Collection<String> instanceIds);
    
}
