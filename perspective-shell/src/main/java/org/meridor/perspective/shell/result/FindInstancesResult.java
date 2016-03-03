package org.meridor.perspective.shell.result;

import org.meridor.perspective.config.CloudType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.meridor.perspective.shell.repository.impl.TextUtils.DASH;
import static org.meridor.perspective.shell.repository.impl.TextUtils.humanizedDuration;

public class FindInstancesResult {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    
    private final String id;
    private final String realId;
    private final String name;
    private final String projectId;
    private final String projectName;
    private final String cloudId;
    private final CloudType cloudType;
    private final String imageName;
    private final String flavorName;
    private final String addresses;
    private final String state;
    private final String lastUpdated;

    public FindInstancesResult(String id, String realId, String name, String projectId, String projectName, String cloudId, String cloudType, String imageName, String flavorName, String addresses, String state, String lastUpdated) {
        this.id = id;
        this.realId = realId;
        this.name = name;
        this.projectId = projectId;
        this.projectName = projectName;
        this.cloudId = cloudId;
        this.cloudType = CloudType.valueOf(cloudType.toUpperCase());
        this.imageName = imageName;
        this.flavorName = flavorName;
        this.addresses = addresses;
        this.state = state;
        this.lastUpdated = lastUpdated;
    }

    public String getRealId() {
        return realId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getCloudId() {
        return cloudId;
    }

    public CloudType getCloudType() {
        return cloudType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProjectName() {
        return projectName != null ?
                projectName : DASH;
    }

    public String getImageName() {
        return imageName != null ?
                imageName : DASH;
    }

    public String getFlavorName() {
        return flavorName != null ?
                flavorName : DASH;
    }

    public String getAddresses() {
        return addresses != null ?
                addresses : DASH;
    }

    public String getState() {
        return state != null ?
                state : DASH;
    }

    public String getLastUpdated() {
        return humanizedDuration(ZonedDateTime.parse(lastUpdated, DATE_TIME_FORMATTER));
    }
}
