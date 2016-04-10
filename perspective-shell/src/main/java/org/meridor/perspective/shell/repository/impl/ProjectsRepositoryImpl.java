package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.shell.request.FindFlavorsRequest;
import org.meridor.perspective.shell.request.FindKeypairsRequest;
import org.meridor.perspective.shell.request.FindNetworksRequest;
import org.meridor.perspective.shell.request.FindProjectsRequest;
import org.meridor.perspective.shell.result.FindFlavorsResult;
import org.meridor.perspective.shell.result.FindKeypairsResult;
import org.meridor.perspective.shell.result.FindNetworksResult;
import org.meridor.perspective.shell.result.FindProjectsResult;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ProjectsRepositoryImpl implements ProjectsRepository {

    @Autowired
    private QueryRepository queryRepository;

    @Override 
    public List<FindProjectsResult> findProjects(FindProjectsRequest findProjectsRequest) {
        QueryResult projectsResult = queryRepository.query(findProjectsRequest.getPayload());
        Data data = projectsResult.getData();
        return data.getRows().stream()
                .map(r -> {
                    ValueFormatter vf = new ValueFormatter(data, r);
                    return new FindProjectsResult(
                            vf.getString("id"),
                            vf.getString("name"),
                            vf.getString("cloud_id"),
                            vf.getString("cloud_type")
                    );
                })
                .collect(Collectors.toList());
    }
    
    @Override 
    public List<FindFlavorsResult> findFlavors(FindFlavorsRequest findFlavorsRequest) {
        QueryResult flavorsResult = queryRepository.query(findFlavorsRequest.getPayload());
        Data data = flavorsResult.getData();
        return data.getRows().stream()
                .map(r -> {
                    ValueFormatter vf = new ValueFormatter(data, r);
                    return new FindFlavorsResult(
                            vf.getString("flavors.id"),
                            vf.getString("flavors.name"),
                            vf.getString("projects.name"),
                            vf.getString("flavors.vcpus"),
                            vf.getString("flavors.ram"),
                            vf.getString("flavors.root_disk"),
                            vf.getString("flavors.ephemeral_disk")
                    );
                })
                .collect(Collectors.toList());
    }
    
    @Override 
    public List<FindNetworksResult> findNetworks(FindNetworksRequest findNetworksRequest) {
        QueryResult networksResult = queryRepository.query(findNetworksRequest.getPayload());
        Data data = networksResult.getData();
        Map<String, FindNetworksResult> resultsMap = new LinkedHashMap<>();
        data.getRows().stream()
                .forEach(r -> {
                    ValueFormatter vf = new ValueFormatter(data, r);
                    String networkId = vf.getString("networks.id");
                    FindNetworksResult findNetworksResult = resultsMap.getOrDefault(networkId, new FindNetworksResult(
                            vf.getString("networks.id"),
                            vf.getString("networks.name"),
                            vf.getString("projects.name"),
                            vf.getString("networks.state"),
                            Boolean.valueOf(vf.getString("networks.is_shared")))
                    );
                    String cidr = vf.getString("network_subnets.cidr");
                    findNetworksResult.getSubnets().add(cidr);
                    resultsMap.put(networkId, findNetworksResult);
                });
        return new ArrayList<>(resultsMap.values());
    }

    @Override
    public List<FindKeypairsResult> findKeypairs(FindKeypairsRequest findKeypairsRequest) {
        QueryResult keypairsResult = queryRepository.query(findKeypairsRequest.getPayload());
        Data data = keypairsResult.getData();
        return data.getRows().stream()
                .map(r -> {
                        ValueFormatter vf = new ValueFormatter(data, r);
                        return new FindKeypairsResult(
                                vf.getString("keypairs.name"),
                                vf.getString("keypairs.fingerprint"),
                                vf.getString("projects.name")
                        );
                })
                .collect(Collectors.toList());
    }

}
