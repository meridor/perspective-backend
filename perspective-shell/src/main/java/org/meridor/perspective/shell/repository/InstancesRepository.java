package org.meridor.perspective.shell.repository;

import org.meridor.perspective.shell.request.AddInstancesRequest;
import org.meridor.perspective.shell.request.FindInstancesRequest;
import org.meridor.perspective.shell.result.FindInstancesResult;
import org.springframework.shell.ShellException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public interface InstancesRepository {
    
    List<FindInstancesResult> findInstances(FindInstancesRequest findInstancesRequest);
    
    Map<String, Map<String, String>> getInstancesMetadata(FindInstancesRequest findInstancesRequest);

    Set<String> addInstances(AddInstancesRequest addInstancesRequest);

    Set<String> deleteInstances(Collection<String> instanceIds);

    Set<String> rebootInstances(Collection<String> instanceIds);

    Set<String> hardRebootInstances(Collection<String> instanceIds);
}
