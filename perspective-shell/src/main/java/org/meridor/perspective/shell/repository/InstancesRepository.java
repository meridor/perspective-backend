package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.query.ShowInstancesQuery;

import java.util.List;
import java.util.Set;

public interface InstancesRepository {
    List<Instance> showInstances(ShowInstancesQuery showInstancesQuery);

    Set<String> addInstances(AddInstancesQuery addInstancesQuery);

    Set<String> deleteInstances(ModifyInstancesQuery modifyInstancesQuery);

    Set<String> rebootInstances(ModifyInstancesQuery modifyInstancesQuery);

    Set<String> hardRebootInstances(ModifyInstancesQuery modifyInstancesQuery);
}
