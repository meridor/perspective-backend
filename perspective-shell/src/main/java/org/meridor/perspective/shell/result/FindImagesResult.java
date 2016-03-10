package org.meridor.perspective.shell.result;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.config.CloudType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.meridor.perspective.shell.repository.impl.TextUtils.DASH;
import static org.meridor.perspective.shell.repository.impl.TextUtils.humanizedDuration;

public class FindImagesResult {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    
    private final String id;
    private final String realId;
    private final String name;
    private final List<String> projectIds = new ArrayList<>();
    private final List<String> projectNames = new ArrayList<>();
    private final String cloudType;
    private final String state;
    private final String lastUpdated;

    public FindImagesResult(String id, String realId, String name, String cloudType, String state, String lastUpdated) {
        this.id = id;
        this.realId = realId;
        this.name = name;
        this.cloudType = cloudType;
        this.state = state;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public String getRealId() {
        return realId;
    }

    public String getName() {
        return name;
    }

    public List<String> getProjectIds() {
        return projectIds;
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    public CloudType getCloudType() {
        return CloudType.fromValue(cloudType);
    }

    public String getState() {
        return state != null ?
                state : DASH;
    }

    public String getLastUpdated() {
        return humanizedDuration(ZonedDateTime.parse(lastUpdated, DATE_TIME_FORMATTER));
    }
    
    public Image toImage() {
        Image image = new Image();
        image.setId(getId());
        image.setRealId(getRealId());
        image.setName(getName());
        image.getProjectIds().addAll(getProjectIds());
        image.setCloudType(getCloudType());
        return image;
    }
}
