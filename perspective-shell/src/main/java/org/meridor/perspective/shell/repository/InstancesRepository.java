package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.query.AddInstancesQuery;
import org.meridor.perspective.shell.repository.query.ShowInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.*;
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
        apiProvider.getInstancesApi().postXmlAs(instances, String.class);
        return Collections.emptySet();
    }
    
    public Set<String> deleteInstances() {
//        apiProvider.getInstancesApi().delete().postXmlAs(instances, String.class);
        return Collections.emptySet();
    }

}
