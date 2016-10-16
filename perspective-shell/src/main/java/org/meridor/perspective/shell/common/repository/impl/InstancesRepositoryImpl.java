package org.meridor.perspective.shell.common.repository.impl;

import okhttp3.ResponseBody;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.repository.QueryRepository;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.sql.Data;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.SelectQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import retrofit2.Call;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.meridor.perspective.shell.common.repository.ApiProvider.processRequestOrException;
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

    @Override
    public Set<String> addInstances(List<Instance> instances) {
        return executeAction(
                (apiProvider, is) -> apiProvider.getInstancesApi().launch(is),
                instances
        );
    }

    @Override
    public Set<String> startInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().start(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> shutdownInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().shutdown(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> deleteInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().delete(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> rebootInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().reboot(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> rebuildInstances(String imageId, Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().rebuild(imageId, ids),
                instanceIds
        );
    }

    @Override
    public Set<String> resizeInstances(String flavorId, Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().resize(flavorId, ids),
                instanceIds
        );
    }

    @Override
    public Set<String> pauseInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().pause(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> resumeInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().resume(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> suspendInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().suspend(ids),
                instanceIds
        );
    }

    @Override
    public Set<String> hardRebootInstances(Collection<String> instanceIds) {
        return executeAction(
                (apiProvider, ids) -> apiProvider.getInstancesApi().hardReboot(ids),
                instanceIds
        );
    }

    private <T> Set<String> executeAction(BiFunction<ApiProvider, T, Call<ResponseBody>> action, T data) {
        return processRequestOrException(() -> {
            Call<ResponseBody> call = action.apply(apiProvider, data);
            call.execute();
            return Collections.emptySet();
        });
    }

}
