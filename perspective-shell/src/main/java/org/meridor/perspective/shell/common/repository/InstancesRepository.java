package org.meridor.perspective.shell.common.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.result.FindInstancesResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InstancesRepository {
    
    List<FindInstancesResult> findInstances(FindInstancesRequest findInstancesRequest);
    
    Map<String, Map<String, String>> getInstancesMetadata(FindInstancesRequest findInstancesRequest);

    Set<String> addInstances(List<Instance> instances);

    Set<String> deleteInstances(Collection<String> instanceIds);

    Set<String> rebootInstances(Collection<String> instanceIds);

    Set<String> hardRebootInstances(Collection<String> instanceIds);
}
