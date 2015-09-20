package org.meridor.perspective.worker.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGenerator {
    
    @Autowired
    private WorkerMetadata workerMetadata;
    
    public String generate(Class<?> role, String id) {
        String cloudId = workerMetadata.getId();
        String idRole = role.getCanonicalName();
        String seed = String.format("%s-%s-%s", cloudId, idRole, id);
        return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
    }
    
}
