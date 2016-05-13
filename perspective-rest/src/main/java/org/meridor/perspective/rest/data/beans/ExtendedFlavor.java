package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.Flavor;

public class ExtendedFlavor {
    private final String projectId;
    private final Flavor flavor;

    public ExtendedFlavor(String projectId, Flavor flavor) {
        this.projectId = projectId;
        this.flavor = flavor;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getId() {
        return flavor.getId();
    }

    public String getName() {
        return flavor.getName();
    }

    public int getRam() {
        return flavor.getRam();
    }

    public int getVcpus() {
        return flavor.getVcpus();
    }

    public int getRootDisk() {
        return flavor.getRootDisk();
    }

    public int getEphemeralDisk() {
        return flavor.getEphemeralDisk();
    }

    public boolean hasSwap() {
        return flavor.isHasSwap();
    }

    public boolean isPublic() {
        return flavor.isIsPublic();
    }
}
