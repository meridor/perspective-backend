package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.client.*;
import org.meridor.perspective.shell.common.misc.HumanReadableException;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.common.repository.SettingsAware;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Callable;

@Repository
public class ApiProviderImpl implements ApiProvider {

    private static final String API_ENVIRONMENT_VARIABLE = "PERSPECTIVE_API_URL";

    private final SettingsAware settingsAware;

    @Autowired
    public ApiProviderImpl(SettingsAware settingsAware) {
        this.settingsAware = settingsAware;
    }

    @Override
    public InstancesApi getInstancesApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(InstancesApi.class));
    }

    @Override
    public ImagesApi getImagesApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(ImagesApi.class));
    }

    @Override
    public QueryApi getQueryApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(QueryApi.class));
    }

    @Override
    public ServiceApi getServiceApi() {
        return getApiOrException(() -> ApiAware.withUrl(getBaseUri()).get(ServiceApi.class));
    }

    private <T> T getApiOrException(Callable<T> apiSupplier) {
        try {
            return apiSupplier.call();
        } catch (Exception e) {
            throw new HumanReadableException("Wrong API URL");
        }
    }

    @Override
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

}
