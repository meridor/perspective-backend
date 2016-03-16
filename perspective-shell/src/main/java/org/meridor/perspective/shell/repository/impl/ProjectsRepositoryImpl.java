package org.meridor.perspective.shell.repository.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.shell.repository.SettingsAware;
import org.meridor.perspective.shell.request.*;
import org.meridor.perspective.shell.result.FindFlavorsResult;
import org.meridor.perspective.shell.result.FindKeypairsResult;
import org.meridor.perspective.shell.result.FindNetworksResult;
import org.meridor.perspective.shell.result.FindProjectsResult;
import org.meridor.perspective.shell.validator.Setting;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.meridor.perspective.sql.DataUtils.get;

@Repository
public class ProjectsRepositoryImpl implements ProjectsRepository {

    @Autowired
    private QueryRepository queryRepository;
    
    @Autowired
    private SettingsAware settingsAware;
    
    private final List<FindProjectsResult> projectsCache = new ArrayList<>();
    
    @Override 
    public List<FindProjectsResult> findProjects(FindProjectsRequest findProjectsRequest) {
        if (projectsCache.isEmpty() || isProjectsCacheDisabled()) {
            QueryResult projectsResult = queryRepository.query(findProjectsRequest.getPayload());
            Data data = projectsResult.getData();
            List<FindProjectsResult> projects = data.getRows().stream()
                    .map(r -> new FindProjectsResult(
                            valueOf(get(data, r, "id")),
                            valueOf(get(data, r, "name")),
                            valueOf(get(data, r, "cloud_id")),
                            valueOf(get(data, r, "cloud_type"))
                    ))
                    .collect(Collectors.toList());
            projectsCache.addAll(projects);
        }
        return projectsCache;
    }
    
    private boolean isProjectsCacheDisabled() {
        return settingsAware.hasSetting(Setting.DISABLE_PROJECTS_CACHE);
    }
    
    @Override 
    public List<FindFlavorsResult> findFlavors(FindFlavorsRequest findFlavorsRequest) {
        QueryResult flavorsResult = queryRepository.query(findFlavorsRequest.getPayload());
        Data data = flavorsResult.getData();
        return data.getRows().stream()
                .map(r -> new FindFlavorsResult(
                        valueOf(get(data, r, "flavors.id")),
                        valueOf(get(data, r, "flavors.name")),
                        valueOf(get(data, r, "projects.name")),
                        valueOf(get(data, r, "flavors.vcpus")),
                        valueOf(get(data, r, "flavors.ram")),
                        valueOf(get(data, r, "flavors.root_disk")),
                        valueOf(get(data, r, "flavors.ephemeral_disk"))
                ))
                .collect(Collectors.toList());
    }
    
    @Override 
    public List<FindNetworksResult> findNetworks(FindNetworksRequest findNetworksRequest) {
        QueryResult networksResult = queryRepository.query(findNetworksRequest.getPayload());
        Data data = networksResult.getData();
        Map<String, FindNetworksResult> resultsMap = new HashMap<>();
        data.getRows().stream()
                .forEach(r -> {
                    String networkId = valueOf(get(data, r, "networks.id"));
                    FindNetworksResult findNetworksResult = resultsMap.getOrDefault(networkId, new FindNetworksResult(
                            valueOf(get(data, r, "networks.id")),
                            valueOf(get(data, r, "networks.name")),
                            valueOf(get(data, r, "projects.name")),
                            valueOf(get(data, r, "networks.state")),
                            Boolean.valueOf(valueOf(get(data, r, "networks.is_shared")))
                    ));
                    String cidr = valueOf(get(data, r, "network_subnets.cidr"));
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
                .map(r -> new FindKeypairsResult(
                        valueOf(get(data, r, "keypairs.name")),
                        valueOf(get(data, r, "keypairs.fingerprint")),
                        valueOf(get(data, r, "projects.name"))
                ))
                .collect(Collectors.toList());
    }

}
