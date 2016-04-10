package org.meridor.perspective.client;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Collection;

public interface ImagesApi {
    
    @GET("/images/{id}")
    Call<Instance> getById(@Path("id") String instanceId);
    
    @POST("/images")
    Call<ResponseBody> add(@Body Collection<Image> images);
    
    @POST("/images/delete")
    Call<ResponseBody> delete(@Body Collection<String> imageIds);
    
}
