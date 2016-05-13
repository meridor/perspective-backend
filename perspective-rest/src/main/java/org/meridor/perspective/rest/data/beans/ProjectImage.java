package org.meridor.perspective.rest.data.beans;

public class ProjectImage {
    private final String projectId;
    private final String imageId;

    public ProjectImage(String projectId, String imageId) {
        this.projectId = projectId;
        this.imageId = imageId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getImageId() {
        return imageId;
    }
}
