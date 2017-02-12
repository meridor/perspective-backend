package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.Subnet;

public class ExtendedNetworkSubnet {
    private final String projectId;
    private final String networkId;
    private final Subnet subnet;

    public ExtendedNetworkSubnet(String projectId, String networkId, Subnet subnet) {
        this.projectId = projectId;
        this.networkId = networkId;
        this.subnet = subnet;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getId() {
        return subnet.getId();
    }

    public String getName() {
        return subnet.getName();
    }

    public String getCidr() {
        return subnet.getCidr();
    }

    public int getProtocolVersion() {
        return subnet.getProtocolVersion();
    }

    public String getGateway() {
        return subnet.getGateway();
    }

    public boolean isDHCPEnabled() {
        return subnet.isIsDHCPEnabled();
    }
}
