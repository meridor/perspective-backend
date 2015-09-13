package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.query.LaunchInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class InstancesRepository {

    @Autowired
    private ApiProvider apiProvider;

    public List<Instance> listInstances() {
        GenericType<ArrayList<Instance>> instanceListType = new GenericType<ArrayList<Instance>>() {};
        return apiProvider.getInstancesApi().getAsXml(instanceListType);
    }
    
    public Set<String> launchInstances(LaunchInstancesQuery launchInstancesQuery) {
        List<Instance> instances = launchInstancesQuery.getPayload();
        apiProvider.getInstancesApi().postXmlAs(instances, String.class);
        return Collections.emptySet();
    }

}
