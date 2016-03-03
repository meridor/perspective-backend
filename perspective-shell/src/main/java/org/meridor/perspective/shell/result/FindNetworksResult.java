package org.meridor.perspective.shell.result;

import org.meridor.perspective.beans.Network;

import java.util.ArrayList;
import java.util.List;

public class FindNetworksResult {
    
    private final String id;
    private final String name;
    private final String projectName;
    private final List<String> subnets = new ArrayList<>();
    private final String state;
    private final boolean isShared;

    public FindNetworksResult(String id, String name, String projectName, String state, boolean isShared) {
        this.id = id;
        this.name = name;
        this.projectName = projectName;
        this.state = state;
        this.isShared = isShared;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public String getState() {
        return state;
    }

    public boolean isShared() {
        return isShared;
    }
    
    public Network toNetwork() {
        Network network = new Network();
        network.setId(getId());
        network.setName(getName());
        network.setState(getState());
        network.setIsShared(isShared());
        return network;
    }
}
