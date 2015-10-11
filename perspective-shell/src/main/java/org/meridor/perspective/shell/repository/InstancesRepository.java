package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.query.ShowInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class InstancesRepository {

    @Autowired
    private ApiProvider apiProvider;

    public List<Instance> showInstances(ShowInstancesQuery showInstancesQuery) {
        GenericType<ArrayList<Instance>> instanceListType = new GenericType<ArrayList<Instance>>() {};
        List<Instance> instances = apiProvider.getInstancesApi().getAsXml(instanceListType);
        return instances.stream().filter(showInstancesQuery.getPayload()).collect(Collectors.toList());
    }

    public Set<String> addInstances(AddInstancesQuery addInstancesQuery) {
        List<Instance> instances = addInstancesQuery.getPayload();
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(instances) {
        };
        apiProvider.getInstancesApi().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    public Set<String> deleteInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().delete().postXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    public Set<String> rebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().reboot().putXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
    public Set<String> hardRebootInstances(ModifyInstancesQuery modifyInstancesQuery) {
        GenericEntity<List<Instance>> data = new GenericEntity<List<Instance>>(modifyInstancesQuery.getPayload()) {
        };
        apiProvider.getInstancesApi().hardReboot().putXmlAs(data, String.class);
        return Collections.emptySet();
    }
    
}
