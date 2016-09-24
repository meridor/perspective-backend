package org.meridor.perspective.shell.common.repository.impl;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.client.ImagesApi;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.client.QueryApi;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.sql.QueryResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Path;

import java.util.ArrayList;
import java.util.Collection;

import static retrofit2.Response.success;

public class MockApiProvider implements ApiProvider {

    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json");

    private final Collection<QueryResult> queryResults = new ArrayList<>();

    @Override
    public InstancesApi getInstancesApi() {
        return new InstancesApi() {
            @Override
            public Call<Instance> getById(@Path("id") String instanceId) {
                return ok(EntityGenerator.getInstance());
            }

            @Override
            public Call<ResponseBody> launch(@Body Collection<Instance> instances) {
                return ok();
            }

            @Override
            public Call<ResponseBody> start(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> shutdown(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> pause(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> resume(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> suspend(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> resize(@Path("flavorId") String flavorId, @Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> rebuild(@Path("imageId") String imageId, @Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> reboot(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> hardReboot(@Body Collection<String> instanceIds) {
                return ok();
            }

            @Override
            public Call<ResponseBody> delete(@Body Collection<String> instanceIds) {
                return ok();
            }
        };
    }

    @Override
    public ImagesApi getImagesApi() {
        return new ImagesApi() {
            @Override
            public Call<Image> getById(@Path("id") String instanceId) {
                return ok(EntityGenerator.getImage());
            }

            @Override
            public Call<ResponseBody> add(@Body Collection<Image> images) {
                return ok();
            }

            @Override
            public Call<ResponseBody> delete(@Body Collection<String> imageIds) {
                return ok();
            }
        };
    }


    @Override
    public QueryApi getQueryApi() {
        return queries -> new MockCall<>(success(queryResults));
    }

    @Override
    public String getBaseUri() {
        return "http://localhost:8080/";
    }

    public Collection<QueryResult> getQueryResults() {
        return queryResults;
    }

    private static <T> Call<T> ok(T data) {
        return new MockCall<>(success(data));
    }

    private static Call<ResponseBody> ok() {
        return new MockCall<>(success(ResponseBody.create(MEDIA_TYPE, "ok")));
    }
}
