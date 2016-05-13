package org.meridor.perspective.rest.data.beans;

import org.meridor.perspective.beans.Keypair;

public class ExtendedKeypair {

    private final String projectId;
    private final Keypair keypair;

    public ExtendedKeypair(String projectId, Keypair keypair) {
        this.projectId = projectId;
        this.keypair = keypair;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getName() {
        return keypair.getName();
    }

    public String getFingerprint() {
        return keypair.getFingerprint();
    }

    public String getPublicKey() {
        return keypair.getPublicKey();
    }
}
