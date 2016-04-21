package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.client.ApiAware;
import org.meridor.perspective.client.ImagesApi;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.client.QueryApi;
import org.meridor.perspective.shell.common.misc.HumanReadableException;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Callable;

@Repository
public class ApiProvider {
    
    private static final String API_ENVIRONMENT_VARIABLE = "PERSPECTIVE_API_URL";
    public static final String API_SYSTEM_PROPERTY = "perspective.api.url";
    
    @Autowired
    private SettingsAware settingsAware;
    
    public InstancesApi getInstancesApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(InstancesApi.class));
    }
    
    public ImagesApi getImagesApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(ImagesApi.class));
    }
    
    public QueryApi getQueryApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(QueryApi.class));
    }
    
    private <T> T getApiOrException(Callable<T> apiSupplier) {
        try {
            return apiSupplier.call();
        } catch (Exception e) {
            throw new HumanReadableException("Wrong API URL");
        }
    }
    
    public String getBaseUri() {
        if (settingsAware.hasSetting(Setting.API_URL)) {
            return settingsAware.getSettingAs(Setting.API_URL, String.class);
        }
        if (System.getenv().containsKey(API_ENVIRONMENT_VARIABLE)) {
            return System.getenv(API_ENVIRONMENT_VARIABLE);
        }
        if (System.getProperties().containsKey(API_SYSTEM_PROPERTY)) {
            return System.getProperty(API_SYSTEM_PROPERTY);
        }
        return "http://localhost:8080/";
    }

    public static <T> T processRequestOrException(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new HumanReadableException(e);
        }
    }

}
