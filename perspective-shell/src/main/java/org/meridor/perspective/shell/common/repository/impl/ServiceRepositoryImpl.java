package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.common.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.common.repository.ApiProvider.processRequestOrException;

@Component
public class ServiceRepositoryImpl implements ServiceRepository {
    
    private final ApiProvider apiProvider;

    @Autowired
    public ServiceRepositoryImpl(ApiProvider apiProvider) {
        this.apiProvider = apiProvider;
    }

    @Override
    public Map<CloudType, Set<OperationType>> getSupportedOperations() {
        return processRequestOrException(() -> {
            Call<Map<CloudType, Set<OperationType>>> call = apiProvider.getServiceApi().getSupportedOperations();
            return call.execute().body();
        });
    }
}
