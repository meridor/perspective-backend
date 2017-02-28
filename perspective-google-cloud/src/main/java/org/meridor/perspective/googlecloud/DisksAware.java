package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.Disk;
import com.google.cloud.compute.InstanceId;
import org.meridor.perspective.config.Cloud;

import java.util.Collection;

public interface DisksAware {

    Collection<Disk> getDisks(Cloud cloud);

    Collection<Disk> getDiskById(Cloud cloud, InstanceId instanceId);

}
