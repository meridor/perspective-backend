package org.meridor.perspective.worker.misc.impl;

import org.meridor.perspective.config.Cloud;

public class MockCloud extends Cloud {

    public MockCloud() {
        setId("test-id");
        setName("test-name");
        setEndpoint("endpoint");
        setIdentity("identity");
        setCredential("credential");
        setEnabled(true);
    }
}
