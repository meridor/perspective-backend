package org.meridor.perspective.worker.misc;

import org.meridor.perspective.config.CloudType;

public interface WorkerMetadata {

    String getId();

    CloudType getCloudType();

}
