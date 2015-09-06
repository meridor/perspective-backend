package org.meridor.perspective.shell.client;

import org.meridor.perspective.client.Perspective;
import org.meridor.perspective.shell.misc.ShellException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.URISyntaxException;

@Repository
public class ApiProvider {
    
    @Value("#{systemProperties['perspective.base.url'] ?: 'http://localhost:8080/'}")
    private String baseUrl;
    
    public Perspective.Projects getProjectsApi() {
        try {
            return Perspective.projects(Perspective.createClient(), new URI(baseUrl));
        } catch (URISyntaxException e) {
            throw new ShellException("Failed to fetch projects", e);
        }
    }
    
}
