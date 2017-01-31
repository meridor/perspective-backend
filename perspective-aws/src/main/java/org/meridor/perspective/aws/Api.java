package org.meridor.perspective.aws;

public interface Api {

    boolean rebootInstance(String instanceId);

    boolean startInstance(String instanceId);

    boolean shutdownInstance(String instanceId);

    boolean deleteInstance(String instanceId);

    void close();
    
}
