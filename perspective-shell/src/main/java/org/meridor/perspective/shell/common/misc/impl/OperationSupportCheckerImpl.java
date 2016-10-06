package org.meridor.perspective.shell.common.misc.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.shell.common.misc.OperationSupportChecker;
import org.meridor.perspective.shell.common.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OperationSupportCheckerImpl implements OperationSupportChecker {

    private final LoadingCache<CloudType, Set<OperationType>> operationsCache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<CloudType, Set<OperationType>>() {
                        public Set<OperationType> load(CloudType cloudType) throws Exception {
                            Map<CloudType, Set<OperationType>> supportedOperations = serviceRepository.getSupportedOperations();
                            return supportedOperations.getOrDefault(cloudType, Collections.emptySet());
                        }
                    }
            );

    private final ServiceRepository serviceRepository;

    @Autowired
    public OperationSupportCheckerImpl(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Override
    public boolean isOperationSupported(CloudType cloudType, OperationType operationType) {
        try {
            return operationsCache.get(cloudType).contains(operationType);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Collection<T> filter(Collection<T> input, Function<T, CloudType> cloudTypeProvider, OperationType operationType) {
        return input.stream()
                .filter(elem -> {
                    CloudType cloudType = cloudTypeProvider.apply(elem);
                    return isOperationSupported(cloudType, operationType);
                })
                .collect(Collectors.toList());
    }
}
