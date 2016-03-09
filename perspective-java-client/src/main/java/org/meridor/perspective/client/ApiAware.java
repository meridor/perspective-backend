package org.meridor.perspective.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.meridor.perspective.api.ObjectMapperFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

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
        ObjectMapper objectMapper = ObjectMapperFactory.createDefaultMapper();
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .client(httpClient).build();
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
                .readTimeout(2, TimeUnit.MINUTES)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();
    }
    
}
