package org.meridor.perspective.shell.result;

public class FindKeypairsResult {
    
    private final String name;
    private final String fingerprint;
    private final String projectName;

    public FindKeypairsResult(String name, String fingerprint, String projectName) {
        this.name = name;
        this.fingerprint = fingerprint;
        this.projectName = projectName;
    }

    public String getName() {
        return name;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getProjectName() {
        return projectName;
    }
}
