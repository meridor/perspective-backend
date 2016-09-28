package org.meridor.perspective.shell.common.result;

import org.meridor.perspective.config.CloudType;

public class FindProjectsResult {
    
    private final String id;
    private final String name;
    private final String cloudId;
    private final String cloudType;
    private final String quota;

    public FindProjectsResult(String id, String name, String cloudId, String cloudType, String quota) {
        this.id = id;
        this.name = name;
        this.cloudId = cloudId;
        this.cloudType = cloudType;
        this.quota = quota;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCloudId() {
        return cloudId;
    }

    public CloudType getCloudType() {
        return CloudType.fromValue(cloudType);
    }

    public String getQuota() {
        return quota;
    }
}
