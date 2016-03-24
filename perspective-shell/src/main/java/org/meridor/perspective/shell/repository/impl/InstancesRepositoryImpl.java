package org.meridor.perspective.shell.repository.impl;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.ApiProvider;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.QueryRepository;
import org.meridor.perspective.shell.request.AddInstancesRequest;
import org.meridor.perspective.shell.request.FindInstancesRequest;
import org.meridor.perspective.shell.result.FindInstancesResult;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import retrofit2.Call;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.meridor.perspective.shell.repository.ApiProvider.processRequestOrException;
import static org.meridor.perspective.sql.DataUtils.get;

@Repository
public class InstancesRepositoryImpl implements InstancesRepository {

    @Autowired
    private ApiProvider apiProvider;
    
    @Autowired
    private QueryRepository queryRepository;

    @Override public List<FindInstancesResult> findInstances(FindInstancesRequest findInstancesRequest) {
        QueryResult instancesResult = queryRepository.query(findInstancesRequest.getPayload());
        Data data = instancesResult.getData();
        return data.getRows().stream()
                .map(r -> {
                    ValueFormatter vf = new ValueFormatter(data , r);
                    return new FindInstancesResult(
                            vf.getString("instances.id"),
                            vf.getString("instances.real_id"),
                            vf.getString("instances.name"),
                            vf.getString("projects.id"),
                            vf.getString("projects.name"),
                            vf.getString("instances.cloud_id"),
                            vf.getString("instances.cloud_type"),
                            vf.getString("images.name"),
                            vf.getString("flavors.name"),
                            vf.getString("instances.addresses"),
                            vf.getString("instances.state"),
                            vf.getString("instances.last_updated")
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Map<String, String>> getInstancesMetadata(FindInstancesRequest findInstancesRequest) {
        QueryResult instancesResult = queryRepository.query(findInstancesRequest.getPayload());
        Data instancesData = instancesResult.getData();
        Map<String, String> instanceIdsNames = instancesData.getRows().stream()
                .collect(Collectors.toMap(
                        r -> valueOf(get(instancesData, r, "instances.id")), 
                        r -> valueOf(get(instancesData, r, "instances.name")) 
                ));
        Map<String, Map<String, String>> instancesMetadata = new HashMap<>();
        Set<String> instanceIds = instanceIdsNames.keySet();
        if (!instanceIds.isEmpty()) {
            Query query = new SelectQuery()
                    .all()
                    .from()
                    .table("instance_metadata")
                    .where()
                    .in("instance_id", instanceIds)
                    .getQuery();
            
            QueryResult metadataResult = queryRepository.query(query);
            Data metadataData = metadataResult.getData();
            metadataData.getRows().forEach(r -> {
                ValueFormatter vf = new ValueFormatter(metadataData, r);
                String instanceId = vf.getString("instance_id");
                String instanceName = instanceIdsNames.get(instanceId);
                String key = vf.getString("key");
                String value = vf.getString("value");
                instancesMetadata.compute(instanceName, (k, ov) -> new HashMap<String, String>(){
                    {
                        if (ov != null) {
                            putAll(ov);
                        }
                        put(key, value);
                    }
                });
            });
        }
        return instancesMetadata;
    }

    @Override public Set<String> addInstances(AddInstancesRequest addInstancesRequest) {
        return processRequestOrException(() -> {
            List<Instance> instances = addInstancesRequest.getPayload();
            Call<Collection<Instance>> call = apiProvider.getInstancesApi().launch(instances);
            call.execute();
            return Collections.emptySet();
        });
    }
    
    @Override public Set<String> deleteInstances(Collection<String> instanceIds) {
        return processRequestOrException(() -> {
            Call<ResponseBody> call = apiProvider.getInstancesApi().delete(instanceIds);
            call.execute();
            return Collections.emptySet();
        });
    }
    
    @Override public Set<String> rebootInstances(Collection<String> instanceIds) {
        return processRequestOrException(() -> {
            Call<ResponseBody> call = apiProvider.getInstancesApi().reboot(instanceIds);
            call.execute();
            return Collections.emptySet();
        });
    }
    
    @Override public Set<String> hardRebootInstances(Collection<String> instanceIds) {
        return processRequestOrException(() -> {
            Call<ResponseBody> call = apiProvider.getInstancesApi().hardReboot(instanceIds);
            call.execute();
            return Collections.emptySet();
        });
    }

}
