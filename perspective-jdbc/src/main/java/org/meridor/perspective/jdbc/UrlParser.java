package org.meridor.perspective.jdbc;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlParser {
    
    private final boolean isValid;
    private URL url;
    
    public UrlParser(String url) {
        this.isValid = parse(url);
    }
    
    private boolean parse(String url) {
        try {
            this.url = new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public String getHost() {
        return url.getHost();
    }
    
    public int getPort() {
        return url.getPort();
    }

    private String[] getUserData() {
        return (url.getUserInfo() != null) ?
                url.getUserInfo().split(":") :
                new String[]{null, null};
    }
    
    public String getUserName() {
        return getUserData()[0];
    }
    
    public String getPassword() {
        return getUserData()[1];
    }
    
}
