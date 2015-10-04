package org.meridor.perspective.shell.repository;

import org.meridor.perspective.client.Perspective;
import org.meridor.perspective.shell.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.ShellException;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.Callable;

@Repository
public class ApiProvider {
    
    @Autowired
    private SettingsAware settingsAware;
    
    public Perspective.Projects getProjectsApi() {
        return getApiOrException(() -> Perspective.projects(Perspective.createClient(), new URI(getBaseUri())));
    }
    
    public Perspective.Instances getInstancesApi() {
        return getApiOrException(() -> Perspective.instances(Perspective.createClient(), new URI(getBaseUri())));
    }
    
    public Perspective.Images getImagesApi() {
        return getApiOrException(() -> Perspective.images(Perspective.createClient(), new URI(getBaseUri())));
    }
    
    private <T> T getApiOrException(Callable<T> apiSupplier) {
        try {
            return apiSupplier.call();
        } catch (Exception e) {
            throw new ShellException("Wrong base URL", e);
        }
    }
    
    private String getBaseUri() {
        if (settingsAware.hasSetting(Setting.API_URL)) {
            return settingsAware.getSettingAs(Setting.API_URL, String.class);
        }
        return "http://localhost:8080/";
    }
    
}
