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
                .map(r -> new FindInstancesResult(
                        valueOf(get(data, r, "instances.id")),
                        valueOf(get(data, r, "instances.real_id")),
                        valueOf(get(data, r, "instances.name")),
                        valueOf(get(data, r, "projects.id")),
                        valueOf(get(data, r, "projects.name")),
                        valueOf(get(data, r, "instances.cloud_id")),
                        valueOf(get(data, r, "instances.cloud_type")),
                        valueOf(get(data, r, "images.name")),
                        valueOf(get(data, r, "flavors.name")),
                        valueOf(get(data, r, "instances.addresses")),
                        valueOf(get(data, r, "instances.state")),
                        valueOf(get(data, r, "instances.last_updated"))
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Map<String, String>> getInstancesMetadata(FindInstancesRequest findInstancesRequest) {
        QueryResult instancesResult = queryRepository.query(findInstancesRequest.getPayload());
        Data instancesData = instancesResult.getData();
        Set<String> instanceIds = instancesData.getRows().stream()
                .map(r -> valueOf(get(instancesData, r, "instances.id")))
                .collect(Collectors.toSet());
        Map<String, Map<String, String>> instancesMetadata = new HashMap<>();
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
                String instanceId = valueOf(get(metadataData, r, "instance_id"));
                String key = valueOf(get(metadataData, r, "key"));
                String value = valueOf(get(metadataData, r, "value"));
                instancesMetadata.compute(instanceId, (k, ov) -> new HashMap<String, String>(){
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
