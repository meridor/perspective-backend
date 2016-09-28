package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.Quota;

public class ExtendedQuota {
    
    private final String projectId;
    private final Quota quota;

    public ExtendedQuota(String projectId, Quota quota) {
        this.projectId = projectId;
        this.quota = quota;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getInstances() {
        return quota.getInstances();
    }

    public String getVcpus() {
        return quota.getVcpus();
    }

    public String getRam() {
        return quota.getRam();
    }

    public String getDisk() {
        return quota.getDisk();
    }

    public String getIps() {
        return quota.getIps();
    }

    public String getSecurityGroups() {
        return quota.getSecurityGroups();
    }

    public String getVolumes() {
        return quota.getVolumes();
    }

    public String getKeypairs() {
        return quota.getKeypairs();
    }
}
