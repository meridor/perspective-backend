package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.Disk;
import com.google.cloud.compute.InstanceId;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.meridor.perspective.config.Cloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class DisksAwareImpl implements DisksAware {

    private final LoadingCache<Cloud, Map<InstanceId, Set<Disk>>> disksCache = CacheBuilder.newBuilder()
            .concurrencyLevel(4)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Cloud, Map<InstanceId, Set<Disk>>>() {
                        @Override
                        public Map<InstanceId, Set<Disk>> load(Cloud cloud) throws Exception {
                            return loadDisks(cloud);
                        }
                    }
            );

    private final ApiProvider apiProvider;

    @Autowired
    public DisksAwareImpl(ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public Collection<Disk> getDisks(Cloud cloud) {
        try {
            return this.disksCache.get(cloud).values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }

    private Map<InstanceId, Set<Disk>> loadDisks(Cloud cloud) {
        Map<InstanceId, Set<Disk>> disks = new HashMap<>();
        Api api = apiProvider.getApi(cloud);
        api.listDisks().forEach(
                d -> d.getAttachedInstances().forEach(
                        id -> {
                            disks.putIfAbsent(id, new HashSet<>());
                            disks.get(id).add(d);
                        }
                )
        );
        return disks;
    }

    @Override
    public Collection<Disk> getDiskById(Cloud cloud, InstanceId instanceId) {
        try {
            return disksCache.get(cloud).getOrDefault(instanceId, Collections.emptySet());
        } catch (ExecutionException e) {
            return Collections.emptyList();
        }
    }
}
