package org.meridor.perspective.shell.repository;

import org.meridor.perspective.client.Perspective;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.ShellException;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.concurrent.Callable;

@Repository
public class ApiProvider {
    
    @Value("#{systemProperties['perspective.base.url'] ?: 'http://localhost:8080/'}")
    private String baseUrl;
    
    public Perspective.Projects getProjectsApi() {
        return getApiOrException(() -> Perspective.projects(Perspective.createClient(), new URI(baseUrl)));
    }
    
    public Perspective.Instances getInstancesApi() {
        return getApiOrException(() -> Perspective.instances(Perspective.createClient(), new URI(baseUrl)));
    }
    
    public Perspective.Images getImagesApi() {
        return getApiOrException(() -> Perspective.images(Perspective.createClient(), new URI(baseUrl)));
    }
    
    private <T> T getApiOrException(Callable<T> apiSupplier) {
        try {
            return apiSupplier.call();
        } catch (Exception e) {
            throw new ShellException(String.format("Wrong base URL: %s", baseUrl), e);
        }
    }
    
}
