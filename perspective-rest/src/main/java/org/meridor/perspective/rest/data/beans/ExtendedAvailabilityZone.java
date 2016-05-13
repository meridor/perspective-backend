package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.AvailabilityZone;

public class ExtendedAvailabilityZone {
    private final String projectId;
    private final AvailabilityZone availabilityZone;

    public ExtendedAvailabilityZone(String projectId, AvailabilityZone availabilityZone) {
        this.projectId = projectId;
        this.availabilityZone = availabilityZone;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getName() {
        return availabilityZone.getName();
    }
}
