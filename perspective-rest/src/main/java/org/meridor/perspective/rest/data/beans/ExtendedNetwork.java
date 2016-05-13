package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.Network;

public class ExtendedNetwork {
    private final String projectId;
    private final Network network;

    public ExtendedNetwork(String projectId, Network network) {
        this.projectId = projectId;
        this.network = network;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getId() {
        return network.getId();
    }

    public String getName() {
        return network.getName();
    }

    public String getState() {
        return network.getState();
    }

    public boolean isShared() {
        return network.isIsShared();
    }
}
