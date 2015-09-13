package org.meridor.perspective.shell.repository;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.query.LaunchInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.core.GenericType;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class InstancesRepository {

    @Autowired
    private ApiProvider apiProvider;

    public List<Instance> listInstances(
            Optional<String> instanceName,
            Optional<String> flavor,
            Optional<String> image,
            Optional<String> state,
            Optional<String> cloud
    ) {
        GenericType<ArrayList<Instance>> instanceListType = new GenericType<ArrayList<Instance>>() {};
        List<Instance> instances = apiProvider.getInstancesApi().getAsXml(instanceListType);
        
        return instances.stream().filter(getInstancePredicate(
                instanceName,
                flavor,
                image,
                state,
                cloud
        )).collect(Collectors.toList());
    }

    private Predicate<Instance> getInstancePredicate(
            Optional<String> instanceName,
            Optional<String> flavor,
            Optional<String> image,
            Optional<String> state,
            Optional<String> cloud
    ) {
        return instance ->
                ( !instanceName.isPresent() || instance.getName().contains(instanceName.get()) ) &&
                ( !flavor.isPresent() || instance.getFlavor().getName().contains(flavor.get()) ) &&
                ( !image.isPresent() || instance.getImage().getName().contains(image.get()) ) &&
                ( !state.isPresent() || instance.getState().value().contains(state.get().toUpperCase()) ) &&
                ( !cloud.isPresent() || instance.getCloudType().value().contains(cloud.get()));
    }


    public Set<String> launchInstances(LaunchInstancesQuery launchInstancesQuery) {
        List<Instance> instances = launchInstancesQuery.getPayload();
        apiProvider.getInstancesApi().postXmlAs(instances, String.class);
        return Collections.emptySet();
    }

}
