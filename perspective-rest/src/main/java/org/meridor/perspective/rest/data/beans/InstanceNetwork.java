package org.meridor.perspective.rest.data.beans;

public class InstanceNetwork {
    private final String instanceId;
    private final String networkId;

    public InstanceNetwork(String instanceId, String networkId) {
        this.instanceId = instanceId;
        this.networkId = networkId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getNetworkId() {
        return networkId;
    }
}
