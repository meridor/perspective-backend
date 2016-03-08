package org.meridor.perspective.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiAware {
    
    private final String baseUrl;
    private final OkHttpClient httpClient;

    private ApiAware(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = createClient();
    }

    public static ApiAware withUrl(String url) {
        return new ApiAware(url);
    }

    public <S> S get(Class<S> serviceClass) {
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(httpClient).build();
        return retrofit.create(serviceClass);
    }
    
    private OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Accept", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }
    
}
