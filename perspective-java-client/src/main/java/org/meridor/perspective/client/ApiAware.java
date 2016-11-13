package org.meridor.perspective.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.meridor.perspective.api.SerializationUtils.createDefaultMapper;

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
        ObjectMapper objectMapper = createDefaultMapper();
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .client(httpClient).build();
        return retrofit.create(serviceClass);
    }

    public String getWebSocketUrl(String endpoint) {
        try {
            URL url = new URL(baseUrl);
            return url.getPort() != -1 ?
                    String.format("ws://%s:%d/%s", url.getHost(), url.getPort(), endpoint) :
                    String.format("ws://%s/%s", url.getHost(), endpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
