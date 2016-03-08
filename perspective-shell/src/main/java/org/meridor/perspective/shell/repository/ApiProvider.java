package org.meridor.perspective.shell.repository;

import org.meridor.perspective.client.ApiAware;
import org.meridor.perspective.client.ImagesApi;
import org.meridor.perspective.client.InstancesApi;
import org.meridor.perspective.client.QueryApi;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.ShellException;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Callable;

@Repository
public class ApiProvider {
    
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
            throw new ShellException("Wrong base URL", e);
        }
    }
    
    public String getBaseUri() {
        if (settingsAware.hasSetting(Setting.API_URL)) {
            return settingsAware.getSettingAs(Setting.API_URL, String.class);
        }
        return "http://localhost:8080/";
    }

    public static <T> T processRequestOrException(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new ShellException("Failed to process request to API", e);
        }
    }

}
